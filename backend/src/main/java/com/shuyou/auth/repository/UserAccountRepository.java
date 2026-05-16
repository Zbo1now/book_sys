package com.shuyou.auth.repository;

import com.shuyou.auth.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByUsername(String username);
  Optional<UserAccount> findByEmail(String email);
  Optional<UserAccount> findByUsernameOrEmail(String username, String email);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);

  @Query("""
    select u from UserAccount u
    where (
      :keyword is null or :keyword = ''
      or u.username like concat('%', :keyword, '%')
      or u.email like concat('%', :keyword, '%')
    )
    and (:status is null or u.status = :status)
    """)
  Page<UserAccount> search(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);
}
