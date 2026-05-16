package com.shuyou.auth.repository;

import com.shuyou.auth.entity.Booklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BooklistRepository extends JpaRepository<Booklist, Long> {
  Optional<Booklist> findByCode(String code);
  Page<Booklist> findByCreatorId(Long creatorId, Pageable pageable);
  Page<Booklist> findByIsPublicTrue(Pageable pageable);

  @Query("""
    select b from Booklist b
    where (:keyword is null or :keyword = '' or b.title like concat('%', :keyword, '%'))
    """)
  Page<Booklist> search(@Param("keyword") String keyword, Pageable pageable);
}
