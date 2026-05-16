package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ActionRequest;
import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.dto.ProfileUpdateRequest;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.UserAuthService;
import com.shuyou.auth.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;
  private final UserAuthService userAuthService;

  public UserController(UserService userService, UserAuthService userAuthService) {
    this.userService = userService;
    this.userAuthService = userAuthService;
  }

  @GetMapping("/{userId}")
  public ApiResponse<Map<String, Object>> getUser(@PathVariable String userId) {
    return ApiResponse.ok(userService.getUser(parseUserId(userId)));
  }

  @GetMapping("/profile/me")
  public ApiResponse<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(userService.me(me));
  }

  @PutMapping("/profile")
  public ApiResponse<Map<String, Object>> updateProfile(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                         @RequestBody ProfileUpdateRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(userService.updateProfile(me, request));
  }

  @GetMapping("/{userId}/booklists")
  public ApiResponse<Map<String, Object>> userBooklists(@PathVariable String userId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "12") int pageSize) {
    return ApiResponse.ok(userService.userBooklists(parseUserId(userId), page, pageSize));
  }

  @PostMapping("/{userId}/follow")
  public ApiResponse<Map<String, Object>> follow(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String userId,
                                                  @RequestBody ActionRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(userService.follow(me, parseUserId(userId), request.action()));
  }

  @GetMapping("/{userId}/following")
  public ApiResponse<Map<String, Object>> following(@PathVariable String userId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
    return ApiResponse.ok(userService.following(parseUserId(userId), page, pageSize));
  }

  @GetMapping("/{userId}/followers")
  public ApiResponse<Map<String, Object>> followers(@PathVariable String userId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
    return ApiResponse.ok(userService.followers(parseUserId(userId), page, pageSize));
  }

  @GetMapping("/activity")
  public ApiResponse<Map<String, Object>> activity(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                   @RequestParam(required = false) String userId) {
    UserAccount me = userAuthService.optionalUser(authorization).orElse(null);
    Long targetId = userId != null && !userId.isBlank() ? parseUserId(userId) : (me != null ? me.getId() : null);
    if (targetId == null) {
      return ApiResponse.ok(Map.of("total", 0, "page", page, "pageSize", pageSize, "items", List.of()));
    }
    return ApiResponse.ok(userService.activity(targetId, page, pageSize));
  }

  @GetMapping("/profile/export")
  public org.springframework.http.ResponseEntity<byte[]> exportProfile(@RequestHeader(value = "Authorization", required = false) String authorization) {
    UserAccount me = userAuthService.requireUser(authorization);
    String json = String.format(
      "{\"username\":\"%s\",\"title\":\"%s\",\"bio\":\"%s\",\"email\":\"%s\",\"joinDate\":\"%s\"}",
      me.getUsername() == null ? "" : me.getUsername().replace("\"", "\\\""),
      me.getTitle() == null ? "" : me.getTitle().replace("\"", "\\\""),
      me.getBio() == null ? "" : me.getBio().replace("\"", "\""),
      me.getEmail() == null ? "" : me.getEmail().replace("\"", "\\\""),
      me.getCreatedAt() == null ? "" : me.getCreatedAt().toString()
    );
    return org.springframework.http.ResponseEntity.ok()
      .header("Content-Type", "application/json;charset=UTF-8")
      .header("Content-Disposition", "attachment; filename=profile.json")
      .body(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  @GetMapping("/search")
  public ApiResponse<Map<String, Object>> searchUsers(
    @RequestHeader(value = "Authorization", required = false) String authorization,
    @RequestParam(defaultValue = "") String keyword,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int pageSize) {
    UserAccount me = userAuthService.optionalUser(authorization).orElse(null);
    Long excludeId = me != null ? me.getId() : null;
    return ApiResponse.ok(userService.searchUsers(keyword, page, pageSize, excludeId));
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
