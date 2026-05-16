package com.shuyou.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RatingRequest(
  @NotNull @DecimalMin("0.0") @DecimalMax("5.0") Double rating,
  String review
) {
}
