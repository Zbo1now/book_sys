package com.shuyou.auth.dto;

public record ActivityUpdateRequest(
  String title,
  String description,
  String cover,
  String startDate,
  String endDate,
  String location,
  Integer maxParticipants,
  String status,
  String approvalStatus
) {
}
