package com.shuyou.auth.repository;

import com.shuyou.auth.entity.BookReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookReviewLikeRepository extends JpaRepository<BookReviewLike, Long> {
  Optional<BookReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);
  
  long countByReviewId(Long reviewId);
  
  void deleteByReviewIdAndUserId(Long reviewId, Long userId);
  
  void deleteByReviewId(Long reviewId);
  
  List<BookReviewLike> findByReviewId(Long reviewId);
}