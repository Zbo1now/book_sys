package com.shuyou.auth.dto;

public record AdminBookUpdateRequest(
  String title,
  String author,
  String tag,
  String description,
  Boolean featured,
  String coverUrl
) {
}
