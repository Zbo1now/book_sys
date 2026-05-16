package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ActivityCreateRequest;
import com.shuyou.auth.dto.ActivityUpdateRequest;
import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.ActivityService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {
  private final UserAuthService userAuthService;
  private final ActivityService activityService;

  public AdminActivityController(UserAuthService userAuthService, ActivityService activityService) {
    this.userAuthService = userAuthService;
    this.activityService = activityService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> list(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int pageSize,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String approvalStatus) {
    userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(activityService.listAll(page, pageSize, status, approvalStatus));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @RequestBody ActivityCreateRequest request) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(activityService.create(admin.getId(), admin.getUsername(), request));
  }

  @PutMapping("/{activityId}")
  public ApiResponse<Map<String, Object>> update(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String activityId,
                                                  @RequestBody ActivityUpdateRequest request) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(activityService.update(activityId, request, admin));
  }

  @DeleteMapping("/{activityId}")
  public ApiResponse<Void> delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                                  @PathVariable String activityId) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    activityService.delete(activityId, admin);
    return ApiResponse.ok();
  }

  @PutMapping("/{activityId}/status")
  public ApiResponse<Map<String, Object>> updateStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @PathVariable String activityId,
                                                        @RequestParam String status) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(activityService.update(activityId, new ActivityUpdateRequest(null, null, null, null, null, null, null, status, null), admin));
  }

  @PutMapping("/{activityId}/approve")
  public ApiResponse<Map<String, Object>> approve(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @PathVariable String activityId,
                                                   @RequestParam String approvalStatus) {
    userAuthService.requireAdmin(authorization);
    if (!"approved".equals(approvalStatus) && !"rejected".equals(approvalStatus)) {
      throw new IllegalArgumentException("审批状态必须是 approved 或 rejected");
    }
    return ApiResponse.ok(activityService.approve(activityId, approvalStatus));
  }
}
