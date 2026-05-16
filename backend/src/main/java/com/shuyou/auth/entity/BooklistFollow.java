package com.shuyou.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "booklist_follow", uniqueConstraints = {
  @UniqueConstraint(name = "uk_booklist_follow", columnNames = {"booklist_code", "user_id"})
})
@Getter
@Setter
public class BooklistFollow {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "booklist_code", nullable = false, length = 64)
  private String booklistCode;

  @Column(name = "user_id", nullable = false)
  private Long userId;
}
