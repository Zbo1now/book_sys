package com.shuyou.auth.repository;

import com.shuyou.auth.entity.BooklistFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BooklistFollowRepository extends JpaRepository<BooklistFollow, Long> {
  long countByBooklistCode(String booklistCode);
  List<BooklistFollow> findByBooklistCode(String booklistCode);
  Optional<BooklistFollow> findByBooklistCodeAndUserId(String booklistCode, Long userId);
  List<BooklistFollow> findByUserId(Long userId);
  void deleteByBooklistCode(String booklistCode);
}
