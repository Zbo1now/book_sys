package com.shuyou.auth.controller;

import com.shuyou.auth.dto.AdminUserUpdateRequest;
import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.AdminService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {
  private final UserAuthService userAuthService;
  private final AdminService adminService;

  public AdminUserController(UserAuthService userAuthService, AdminService adminService) {
    this.userAuthService = userAuthService;
    this.adminService = adminService;
  }

  @GetMapping("/dashboard")
  public ApiResponse<Map<String, Object>> dashboard(@RequestHeader(value = "Authorization", required = false) String authorization) {
    userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(adminService.dashboard());
  }

  @GetMapping("/users")
  public ApiResponse<Map<String, Object>> users(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status) {
    userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(adminService.listUsers(page, pageSize, keyword, status));
  }

  @PutMapping("/users/{userId}")
  public ApiResponse<Map<String, Object>> updateUser(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @PathVariable String userId,
                                                      @RequestBody AdminUserUpdateRequest request) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    Long targetId = parseUserId(userId);
    if (admin.getId().equals(targetId) && request.role() != null && !"ADMIN".equalsIgnoreCase(request.role())) {
      throw new IllegalArgumentException("不能移除自己的管理员角色");
    }
    return ApiResponse.ok(adminService.updateUser(targetId, request));
  }

  @PutMapping("/users/{userId}/status")
  public ApiResponse<Map<String, Object>> updateStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @PathVariable String userId,
                                                        @RequestParam(defaultValue = "1") int status) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    Long targetId = parseUserId(userId);
    if (admin.getId().equals(targetId) && status != 1) {
      throw new IllegalArgumentException("不能禁用当前管理员账号");
    }
    return ApiResponse.ok(adminService.updateUserStatus(targetId, status));
  }

  private Long parseUserId(String userId) {
    String normalized = userId.startsWith("u-") ? userId.substring(2) : userId;
    try {
      return Long.parseLong(normalized);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("用户ID格式错误");
    }
  }
}
