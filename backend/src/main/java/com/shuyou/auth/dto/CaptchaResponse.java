package com.shuyou.auth.dto;

public record CaptchaResponse(String id, String code, String imageBase64) {
}
