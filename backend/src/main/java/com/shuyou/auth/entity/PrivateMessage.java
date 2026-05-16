package com.shuyou.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "private_message", indexes = {
  @Index(name = "idx_pm_sender", columnList = "sender_id"),
  @Index(name = "idx_pm_receiver", columnList = "receiver_id")
})
@Getter
@Setter
public class PrivateMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sender_id", nullable = false)
  private Long senderId;

  @Column(name = "receiver_id", nullable = false)
  private Long receiverId;

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = Instant.now();
  }
}
