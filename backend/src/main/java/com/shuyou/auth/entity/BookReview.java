package com.shuyou.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "book_review", indexes = {
  @Index(name = "idx_book_review_book", columnList = "book_code"),
  @Index(name = "idx_book_review_user", columnList = "user_id")
})
@Getter
@Setter
public class BookReview {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "book_code", nullable = false, length = 64)
  private String bookCode;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "user_name", length = 64)
  private String userName;

  @Column(nullable = false)
  private Double rating;

  @Column(length = 2000)
  private String content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }
}
