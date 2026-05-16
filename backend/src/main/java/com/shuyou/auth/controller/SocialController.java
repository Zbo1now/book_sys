package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.SocialService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/social")
public class SocialController {
  private final SocialService socialService;
  private final UserAuthService userAuthService;

  public SocialController(SocialService socialService, UserAuthService userAuthService) {
    this.socialService = socialService;
    this.userAuthService = userAuthService;
  }

  @PostMapping("/follow/{userId}")
  public ApiResponse<Map<String, Object>> toggleFollow(
    @PathVariable Long userId,
    @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(socialService.toggleFollow(me.getId(), me.getUsername(), userId));
  }

  @GetMapping("/followers/{userId}")
  public ApiResponse<Map<String, Object>> getFollowers(@PathVariable Long userId) {
    return ApiResponse.ok(socialService.getFollowers(userId));
  }

  @GetMapping("/following/{userId}")
  public ApiResponse<Map<String, Object>> getFollowing(@PathVariable Long userId) {
    return ApiResponse.ok(socialService.getFollowing(userId));
  }

  @GetMapping("/is-following/{userId}")
  public ApiResponse<Map<String, Object>> isFollowing(
    @PathVariable Long userId,
    @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    UserAccount me = userAuthService.optionalUser(authorization).orElse(null);
    boolean following = me != null && socialService.isFollowing(me.getId(), userId);
    return ApiResponse.ok(Map.of("following", following));
  }

  @PostMapping("/messages/{userId}")
  public ApiResponse<Map<String, Object>> sendMessage(
    @PathVariable Long userId,
    @RequestBody Map<String, String> body,
    @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    UserAccount me = userAuthService.requireUser(authorization);
    String content = body.get("content");
    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("消息内容不能为空");
    }
    return ApiResponse.ok(socialService.sendMessage(me.getId(), me.getUsername(), userId, content));
  }

  @GetMapping("/messages/{userId}")
  public ApiResponse<Map<String, Object>> getConversation(
    @PathVariable Long userId,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int pageSize,
    @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(socialService.getConversation(me.getId(), userId, page, pageSize));
  }

  @GetMapping("/conversations")
  public ApiResponse<Map<String, Object>> getConversations(
    @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(socialService.getConversations(me.getId()));
  }

  @GetMapping("/unread-count")
  public ApiResponse<Map<String, Object>> getUnreadCount(
    @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(Map.of("count", socialService.getUnreadCount(me.getId())));
  }
}
