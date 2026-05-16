package com.shuyou.auth.repository;

import com.shuyou.auth.entity.BooklistComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BooklistCommentRepository extends JpaRepository<BooklistComment, Long> {
  List<BooklistComment> findByBooklistCodeOrderByCreatedAtDesc(String booklistCode);
  void deleteByBooklistCode(String booklistCode);
  long countByBooklistCode(String booklistCode);
}
