package com.shuyou.auth.controller;

import com.shuyou.auth.dto.AdminLoginRequest;
import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.dto.AuthResponse;
import com.shuyou.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
  private final AuthService authService;

  public AdminAuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
    return ApiResponse.ok(authService.adminLogin(request));
  }
}
