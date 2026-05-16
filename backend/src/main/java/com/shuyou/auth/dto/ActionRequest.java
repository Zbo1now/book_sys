package com.shuyou.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ActionRequest(@NotBlank String action) {
}
