package com.shuyou.auth.controller;

import com.shuyou.auth.dto.AuthResponse;
import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.dto.CaptchaResponse;
import com.shuyou.auth.dto.LoginRequest;
import com.shuyou.auth.dto.RegisterRequest;
import com.shuyou.auth.service.AuthService;
import com.shuyou.auth.service.CaptchaStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final CaptchaStore captchaStore;

  public AuthController(AuthService authService, CaptchaStore captchaStore) {
    this.authService = authService;
    this.captchaStore = captchaStore;
  }

  @GetMapping("/captcha")
  public ApiResponse<CaptchaResponse> captcha() {
    CaptchaStore.CaptchaResult result = captchaStore.generate();
    return ApiResponse.ok(new CaptchaResponse(result.id(), result.code(), result.imageBase64()));
  }

  @PostMapping("/register")
  public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ApiResponse.ok(authService.register(request));
  }

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.ok(authService.login(request));
  }
}
