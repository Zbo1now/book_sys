package com.shuyou.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record BooklistCommentCreateRequest(
  @NotBlank String content
) {
}
