package com.shuyou.auth.repository;

import com.shuyou.auth.entity.BooklistLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BooklistLikeRepository extends JpaRepository<BooklistLike, Long> {
  List<BooklistLike> findByBooklistCode(String booklistCode);
  
  Optional<BooklistLike> findByBooklistCodeAndUserId(String booklistCode, Long userId);
  
  long countByBooklistCode(String booklistCode);
  
  boolean existsByBooklistCodeAndUserId(String booklistCode, Long userId);
  
  void deleteByBooklistCode(String booklistCode);
}
