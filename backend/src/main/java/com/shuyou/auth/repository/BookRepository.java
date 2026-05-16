package com.shuyou.auth.repository;

import com.shuyou.auth.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
  Optional<Book> findByCode(String code);

  Page<Book> findByFeaturedTrue(Pageable pageable);

  @Query("""
    select b from Book b
    where (:tag is null or :tag = '' or b.tag = :tag)
      and (
        :search is null or :search = ''
        or b.title like concat('%', :search, '%')
        or b.author like concat('%', :search, '%')
      )
    """)
  Page<Book> search(@Param("search") String search, @Param("tag") String tag, Pageable pageable);
}
