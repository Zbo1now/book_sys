package com.shuyou.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookReviewRequest(
  @NotNull(message = "评分不能为空")
  @DecimalMin(value = "0.5", message = "评分最低为0.5")
  @DecimalMax(value = "5.0", message = "评分最高为5.0")
  Double rating,

  @Size(max = 2000, message = "评论内容不能超过2000字")
  String content
) {}
