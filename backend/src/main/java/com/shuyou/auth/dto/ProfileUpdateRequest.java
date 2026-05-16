package com.shuyou.auth.dto;

public record ProfileUpdateRequest(
  String username,
  String title,
  String bio,
  String avatar
) {
}
