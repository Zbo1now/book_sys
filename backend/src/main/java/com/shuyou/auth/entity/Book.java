package com.shuyou.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "book")
@Getter
@Setter
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false, length = 128)
  private String title;

  @Column(nullable = false, length = 128)
  private String author;

  @Column(length = 32)
  private String tag;

  @Column(nullable = false)
  private Double rating = 0.0;

  @Column(nullable = false)
  private Integer reviews = 0;

  @Column(name = "cover_url", length = 255)
  private String coverUrl;

  @Column(length = 1000)
  private String description;

  @Column(name = "long_description", length = 4000)
  private String longDescription;

  @Column(name = "author_intro", length = 2000)
  private String authorIntro;

  @Column(length = 64)
  private String language;

  @Column(length = 128)
  private String category;

  @Column(name = "publish_date", length = 32)
  private String publishDate;

  @Column(length = 128)
  private String publisher;

  @Column
  private Integer pages;

  @Column(length = 64)
  private String isbn;

  @Column(nullable = false)
  private Boolean featured = false;

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
