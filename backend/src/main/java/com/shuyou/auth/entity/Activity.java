package com.shuyou.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "activity")
@Getter
@Setter
public class Activity {
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

  @Column(name = "organizer_id", nullable = false)
  private Long organizerId;

  @Column(name = "start_date", length = 64)
  private String startDate;

  @Column(name = "end_date", length = 64)
  private String endDate;

  @Column(length = 128)
  private String location;

  @Column(name = "participant_count", nullable = false)
  private Integer participantCount = 0;

  @Column(name = "max_participants")
  private Integer maxParticipants;

  @Column(nullable = false, length = 32)
  private String status = "upcoming";

  @Column(name = "approval_status", nullable = false, length = 32)
  private String approvalStatus = "pending";

  @Column(name = "activity_type", length = 32)
  private String activityType = "offline";

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
