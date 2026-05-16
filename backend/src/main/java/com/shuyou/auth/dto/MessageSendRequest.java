package com.shuyou.auth.dto;

public record MessageSendRequest(
  String recipientId,
  String recipientEmail,
  String content,
  String conversationId,
  String type
) {
}
