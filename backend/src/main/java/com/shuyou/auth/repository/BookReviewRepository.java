package com.shuyou.auth.repository;

import com.shuyou.auth.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
  List<BookReview> findByBookCodeOrderByCreatedAtDesc(String bookCode);

  Page<BookReview> findByBookCodeOrderByCreatedAtDesc(String bookCode, Pageable pageable);
  
  Page<BookReview> findByBookCode(String bookCode, Pageable pageable);

  List<BookReview> findByBookCodeAndUserId(String bookCode, Long userId);

  List<BookReview> findByUserId(Long userId);
  List<BookReview> findByUserIdOrderByCreatedAtDesc(Long userId);

  @Query("select avg(r.rating) from BookReview r where r.bookCode = :bookCode")
  Double getAverageRatingByBookCode(@Param("bookCode") String bookCode);

  @Query("select count(r) from BookReview r where r.bookCode = :bookCode")
  Long countByBookCode(@Param("bookCode") String bookCode);

  boolean existsByBookCodeAndUserId(String bookCode, Long userId);
}
