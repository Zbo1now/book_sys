package com.shuyou.auth.dto;

import java.util.List;

public record BooklistUpdateRequest(
  String title,
  String description,
  String cover,
  Boolean isPublic,
  List<String> bookIds
) {
}
