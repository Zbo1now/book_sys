package com.shuyou.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity_participant")
@Getter
@Setter
public class ActivityParticipant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "activity_code", length = 64)
  private String activityCode;

  @Column(name = "activity_id", nullable = true)
  private Long activityId;

  @Column(name = "user_id", nullable = false)
  private Long userId;
}
