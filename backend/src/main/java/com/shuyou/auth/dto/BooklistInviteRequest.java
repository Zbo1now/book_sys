package com.shuyou.auth.dto;

public record BooklistInviteRequest(
  String userId,
  String permission
) {
}
