package com.shuyou.auth.dto;

public record AuthResponse(Long userId, String username, String email, String token, String role) {
}
