package com.shuyou.auth.service;

import com.shuyou.auth.entity.PrivateMessage;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.entity.UserFollow;
import com.shuyou.auth.repository.PrivateMessageRepository;
import com.shuyou.auth.repository.UserAccountRepository;
import com.shuyou.auth.repository.UserFollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SocialService {
  private final UserFollowRepository userFollowRepository;
  private final PrivateMessageRepository privateMessageRepository;
  private final UserAccountRepository userAccountRepository;

  public SocialService(UserFollowRepository userFollowRepository,
                       PrivateMessageRepository privateMessageRepository,
                       UserAccountRepository userAccountRepository) {
    this.userFollowRepository = userFollowRepository;
    this.privateMessageRepository = privateMessageRepository;
    this.userAccountRepository = userAccountRepository;
  }

  @Transactional
  public Map<String, Object> toggleFollow(Long followerId, String followerName, Long followingId) {
    if (followerId.equals(followingId)) {
      throw new IllegalArgumentException("不能关注自己");
    }
    
    UserAccount followingUser = userAccountRepository.findById(followingId)
      .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    
    Optional<UserFollow> existing = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId);
    boolean followed;
    
    if (existing.isPresent()) {
      userFollowRepository.delete(existing.get());
      followed = false;
    } else {
      UserFollow follow = new UserFollow();
      follow.setFollowerId(followerId);
      follow.setFollowerName(followerName);
      follow.setFollowingId(followingId);
      follow.setFollowingName(followingUser.getUsername());
      userFollowRepository.save(follow);
      followed = true;
    }
    
    long followerCount = userFollowRepository.countByFollowingId(followingId);
    long followingCount = userFollowRepository.countByFollowerId(followerId);
    
    return Map.of(
      "followed", followed,
      "followerCount", followerCount,
      "followingCount", followingCount
    );
  }

  public Map<String, Object> getFollowers(Long userId) {
    List<UserFollow> follows = userFollowRepository.findByFollowingId(userId);
    List<Long> followerIds = follows.stream().map(UserFollow::getFollowerId).toList();
    
    List<Map<String, Object>> followers = new ArrayList<>();
    if (!followerIds.isEmpty()) {
      Map<Long, UserAccount> userMap = userAccountRepository.findAllById(followerIds).stream()
        .collect(Collectors.toMap(UserAccount::getId, u -> u, (a, b) -> a));
      
      followers = follows.stream()
        .map(f -> {
          UserAccount user = userMap.get(f.getFollowerId());
          return Map.<String, Object>of(
            "id", "u-" + f.getFollowerId(),
            "username", user != null ? user.getUsername() : f.getFollowerName(),
            "followedAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : ""
          );
        })
        .toList();
    }
    
    return Map.of("items", followers, "total", followers.size());
  }

  public Map<String, Object> getFollowing(Long userId) {
    List<UserFollow> follows = userFollowRepository.findByFollowerId(userId);
    List<Long> followingIds = follows.stream().map(UserFollow::getFollowingId).toList();
    
    List<Map<String, Object>> following = new ArrayList<>();
    if (!followingIds.isEmpty()) {
      Map<Long, UserAccount> userMap = userAccountRepository.findAllById(followingIds).stream()
        .collect(Collectors.toMap(UserAccount::getId, u -> u, (a, b) -> a));
      
      following = follows.stream()
        .map(f -> {
          UserAccount user = userMap.get(f.getFollowingId());
          return Map.<String, Object>of(
            "id", "u-" + f.getFollowingId(),
            "username", user != null ? user.getUsername() : f.getFollowingName(),
            "followedAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : ""
          );
        })
        .toList();
    }
    
    return Map.of("items", following, "total", following.size());
  }

  public boolean isFollowing(Long followerId, Long followingId) {
    return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
  }

  @Transactional
  public Map<String, Object> sendMessage(Long senderId, String senderName, Long receiverId, String content) {
    if (senderId.equals(receiverId)) {
      throw new IllegalArgumentException("不能给自己发私信");
    }
    
    UserAccount receiver = userAccountRepository.findById(receiverId)
      .orElseThrow(() -> new IllegalArgumentException("接收者不存在"));
    
    PrivateMessage message = new PrivateMessage();
    message.setSenderId(senderId);
    message.setSenderName(senderName);
    message.setReceiverId(receiverId);
    message.setReceiverName(receiver.getUsername());
    message.setContent(content.trim());
    message.setIsRead(false);
    privateMessageRepository.save(message);
    
    return Map.of(
      "id", message.getId(),
      "senderId", "u-" + senderId,
      "senderName", senderName,
      "receiverId", "u-" + receiverId,
      "receiverName", receiver.getUsername(),
      "content", message.getContent(),
      "createdAt", message.getCreatedAt() != null ? message.getCreatedAt().toString() : "",
      "isRead", false
    );
  }

  public Map<String, Object> getConversation(Long userId, Long otherUserId, int page, int pageSize) {
    Page<PrivateMessage> messages = privateMessageRepository.findConversation(
      userId, otherUserId,
      PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
    );
    
    privateMessageRepository.markAsRead(userId, otherUserId);
    
    List<Map<String, Object>> items = messages.getContent().stream()
      .map(m -> Map.<String, Object>of(
        "id", m.getId(),
        "senderId", "u-" + m.getSenderId(),
        "senderName", m.getSenderName(),
        "receiverId", "u-" + m.getReceiverId(),
        "receiverName", m.getReceiverName(),
        "content", m.getContent(),
        "createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : "",
        "isRead", m.getIsRead() != null && m.getIsRead()
      ))
      .collect(Collectors.toList());
    Collections.reverse(items);
    
    return Map.of(
      "items", items,
      "total", messages.getTotalElements(),
      "page", page,
      "pageSize", pageSize
    );
  }

  public Map<String, Object> getConversations(Long userId) {
    List<Long> partnerIds = privateMessageRepository.findConversationPartnerIds(userId);
    
    List<Map<String, Object>> conversations = new ArrayList<>();
    for (Long partnerId : partnerIds) {
      UserAccount partner = userAccountRepository.findById(partnerId).orElse(null);
      if (partner == null) continue;
      
      long unreadCount = privateMessageRepository.findUnreadByReceiverId(userId).stream()
        .filter(m -> m.getSenderId().equals(partnerId))
        .count();
      
      conversations.add(Map.<String, Object>of(
        "userId", "u-" + partnerId,
        "username", partner.getUsername(),
        "unreadCount", unreadCount
      ));
    }
    
    return Map.of("items", conversations, "total", conversations.size());
  }

  public long getUnreadCount(Long userId) {
    return privateMessageRepository.countUnreadByReceiverId(userId);
  }
}
