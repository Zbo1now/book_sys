package com.shuyou.auth.service;

import com.shuyou.auth.dto.MessageSendRequest;
import com.shuyou.auth.entity.PrivateMessage;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.PrivateMessageRepository;
import com.shuyou.auth.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {
  private final PrivateMessageRepository privateMessageRepository;
  private final UserAccountRepository userAccountRepository;

  public MessageService(PrivateMessageRepository privateMessageRepository,
                        UserAccountRepository userAccountRepository) {
    this.privateMessageRepository = privateMessageRepository;
    this.userAccountRepository = userAccountRepository;
  }

  public Map<String, Object> list(int page, int pageSize, Long userId) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);

    List<Long> partnerIds = privateMessageRepository.findConversationPartnerIds(userId);

    List<Map<String, Object>> items = new ArrayList<>();
    for (Long partnerId : partnerIds) {
      Optional<UserAccount> partnerOpt = userAccountRepository.findById(partnerId);
      if (partnerOpt.isEmpty()) continue;

      UserAccount partner = partnerOpt.get();

      List<PrivateMessage> lastMessages = privateMessageRepository.findConversationAsc(userId, partnerId);
      String lastMessage = "";
      Instant lastTime = null;
      long unreadCount = 0;

      if (!lastMessages.isEmpty()) {
        PrivateMessage lastMsg = lastMessages.get(lastMessages.size() - 1);
        lastMessage = lastMsg.getContent();
        lastTime = lastMsg.getCreatedAt();
        unreadCount = lastMessages.stream()
          .filter(m -> m.getSenderId().equals(partnerId) && !m.getIsRead())
          .count();
      }

      items.add(Map.of(
        "id", "conv-" + partnerId,
        "participant", Map.of(
          "id", "u-" + partnerId,
          "username", partner.getUsername(),
          "email", partner.getEmail() != null ? partner.getEmail() : ""
        ),
        "lastMessage", lastMessage,
        "lastMessageTime", lastTime != null ? lastTime.toString() : "",
        "unreadCount", unreadCount
      ));
    }

    return Map.of("total", items.size(), "page", safePage, "pageSize", safePageSize, "items", items);
  }

  public Map<String, Object> detail(String conversationId, int page, int pageSize, Long userId) {
    Long partnerId = parsePartnerId(conversationId);
    if (partnerId == null) {
      return Map.of("conversationId", conversationId, "participant", Map.of(), "messages", List.of());
    }

    Optional<UserAccount> partnerOpt = userAccountRepository.findById(partnerId);
    Map<String, Object> participant = partnerOpt
      .map(p -> Map.<String, Object>of("id", "u-" + p.getId(), "username", p.getUsername()))
      .orElse(Map.of("id", "u-" + partnerId, "username", "未知用户"));

    List<PrivateMessage> messages = privateMessageRepository.findConversationAsc(userId, partnerId);

    List<Map<String, Object>> msgList = messages.stream()
      .map(m -> Map.<String, Object>of(
        "id", m.getId(),
        "sender", Map.of("id", "u-" + m.getSenderId(), "username", m.getSenderName() != null ? m.getSenderName() : ""),
        "content", m.getContent(),
        "timestamp", m.getCreatedAt().toString(),
        "isMine", m.getSenderId().equals(userId)
      ))
      .collect(Collectors.toList());

    return Map.of("conversationId", conversationId, "participant", participant, "messages", msgList);
  }

  @Transactional
  public Map<String, Object> send(UserAccount sender, MessageSendRequest request) {
    if (request.content() == null || request.content().isBlank()) {
      throw new IllegalArgumentException("消息内容不能为空");
    }

    Long receiverId = null;
    UserAccount receiver = null;

    // 通过邮箱查找收件人
    if (request.recipientEmail() != null && !request.recipientEmail().isBlank()) {
      receiver = userAccountRepository.findByEmail(request.recipientEmail())
        .orElseThrow(() -> new IllegalArgumentException("该邮箱用户不存在"));
      receiverId = receiver.getId();
    }
    // 通过ID查找收件人
    else if (request.recipientId() != null && !request.recipientId().isBlank()) {
      receiverId = parseUserId(request.recipientId());
      receiver = userAccountRepository.findById(receiverId)
        .orElseThrow(() -> new IllegalArgumentException("收件人不存在"));
    }
    // 通过会话ID查找收件人
    else if (request.conversationId() != null && !request.conversationId().isBlank()) {
      receiverId = parsePartnerId(request.conversationId());
      if (receiverId != null) {
        receiver = userAccountRepository.findById(receiverId).orElse(null);
      }
      if (receiverId == null || receiver == null) {
        throw new IllegalArgumentException("会话不存在");
      }
    } else {
      throw new IllegalArgumentException("必须指定收件人");
    }

    PrivateMessage message = new PrivateMessage();
    message.setSenderId(sender.getId());
    message.setSenderName(sender.getUsername());
    message.setReceiverId(receiverId);
    message.setReceiverName(receiver.getUsername());
    message.setContent(request.content());
    message.setIsRead(false);

    privateMessageRepository.save(message);

    return Map.of(
      "id", message.getId(),
      "content", message.getContent(),
      "conversationId", "conv-" + receiverId,
      "timestamp", message.getCreatedAt().toString()
    );
  }

  @Transactional
  public Map<String, Object> readAll(String conversationId, Long userId) {
    Long partnerId = parsePartnerId(conversationId);
    if (partnerId != null) {
      privateMessageRepository.markAsRead(userId, partnerId);
    }
    long unreadCount = privateMessageRepository.countUnreadByReceiverId(userId);
    return Map.of("readCount", unreadCount);
  }

  private Long parsePartnerId(String conversationId) {
    if (conversationId == null || !conversationId.startsWith("conv-")) {
      return null;
    }
    try {
      return Long.parseLong(conversationId.substring(5));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Long parseUserId(String userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户ID不能为空");
    }
    String normalized = userId.startsWith("u-") ? userId.substring(2) : userId;
    try {
      return Long.parseLong(normalized);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("用户ID格式错误");
    }
  }
}
