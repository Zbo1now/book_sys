package com.shuyou.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "booklist")
@Getter
@Setter
public class Booklist {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false, length = 128)
  private String title;

  @Column(length = 1000)
  private String description;

  @Column(name = "cover_url", length = 255)
  private String coverUrl;

  @Column(name = "creator_id", nullable = false)
  private Long creatorId;

  @Column(name = "creator_name", nullable = false, length = 64)
  private String creatorName;

  @Column(name = "book_codes", length = 2000)
  private String bookCodes;

  @Column(name = "book_count", nullable = false)
  private Integer bookCount = 0;

  @Column(name = "follower_count", nullable = false)
  private Integer followerCount = 0;

  @Column(name = "like_count", nullable = false)
  private Integer likeCount = 0;

  @Column(nullable = false)
  private Double rating = 0.0;

  @Column(name = "is_public", nullable = false)
  private Boolean isPublic = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
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
