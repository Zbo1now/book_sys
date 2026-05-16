package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.dto.BooklistCreateRequest;
import com.shuyou.auth.dto.BooklistUpdateRequest;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.BooklistService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/booklists")
public class AdminBooklistController {
  private final UserAuthService userAuthService;
  private final BooklistService booklistService;

  public AdminBooklistController(UserAuthService userAuthService, BooklistService booklistService) {
    this.userAuthService = userAuthService;
    this.booklistService = booklistService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> list(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int pageSize) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(booklistService.list(page, pageSize, "all", admin));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @RequestBody BooklistCreateRequest request) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(booklistService.create(admin.getId(), admin.getUsername(), request));
  }

  @PutMapping("/{booklistId}")
  public ApiResponse<Map<String, Object>> update(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String booklistId,
                                                  @RequestBody BooklistUpdateRequest request) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(booklistService.update(booklistId, request, admin));
  }

  @DeleteMapping("/{booklistId}")
  public ApiResponse<Void> delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                                  @PathVariable String booklistId) {
    UserAccount admin = userAuthService.requireAdmin(authorization);
    booklistService.delete(booklistId, admin);
    return ApiResponse.ok();
  }
}
