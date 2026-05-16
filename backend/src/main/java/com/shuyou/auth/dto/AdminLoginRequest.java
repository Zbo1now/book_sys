package com.shuyou.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
  @NotBlank String account,
  @NotBlank String password
) {
}
