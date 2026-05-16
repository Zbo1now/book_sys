package com.shuyou.auth.dto;

public record ActivityCreateRequest(
  String title,
  String description,
  String cover,
  String startDate,
  String endDate,
  String location,
  Integer maxParticipants,
  String activityType
) {
}
