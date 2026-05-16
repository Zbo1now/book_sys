package com.shuyou.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
  @NotBlank String account,
  @NotBlank String password,
  @NotBlank String captchaId,
  @NotBlank String captchaValue,
  String role
) {
}
