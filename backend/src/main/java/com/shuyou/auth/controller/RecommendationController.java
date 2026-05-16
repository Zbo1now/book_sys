package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.RecommendationService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
  private final RecommendationService recommendationService;
  private final UserAuthService userAuthService;

  public RecommendationController(RecommendationService recommendationService, UserAuthService userAuthService) {
    this.recommendationService = recommendationService;
    this.userAuthService = userAuthService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> getRecommendations(
    @RequestParam(defaultValue = "10") int limit,
    @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    UserAccount user = userAuthService.optionalUser(authorization).orElse(null);
    if (user == null) {
      return ApiResponse.ok(Map.of("items", java.util.List.of(), "reason", "请登录后获取个性化推荐", "total", 0));
    }
    return ApiResponse.ok(recommendationService.getRecommendations(user.getId(), limit));
  }
}
