package com.shuyou.auth.dto;

public record AdminUserUpdateRequest(
  String username,
  String email,
  String title,
  String bio,
  String phone,
  String role
) {
}
