package com.shuyou.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_follow", uniqueConstraints = {
  @UniqueConstraint(name = "uk_user_follow", columnNames = {"follower_id", "following_id"})
})
@Getter
@Setter
public class UserFollow {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "follower_id", nullable = false)
  private Long followerId;

  @Column(name = "follower_name", length = 64)
  private String followerName;

  @Column(name = "following_id", nullable = false)
  private Long followingId;

  @Column(name = "following_name", length = 64)
  private String followingName;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = Instant.now();
  }
}
