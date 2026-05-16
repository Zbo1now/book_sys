package com.shuyou.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "booklist_comment")
@Getter
@Setter
public class BooklistComment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "booklist_code", nullable = false, length = 64)
  private String booklistCode;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "username", nullable = false, length = 64)
  private String username;

  @Column(name = "content", nullable = false, length = 1000)
  private String content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = Instant.now();
  }
}
