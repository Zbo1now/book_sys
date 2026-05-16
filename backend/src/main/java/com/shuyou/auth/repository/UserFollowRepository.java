package com.shuyou.auth.repository;

import com.shuyou.auth.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
  Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
  
  List<UserFollow> findByFollowerId(Long followerId);
  
  List<UserFollow> findByFollowingId(Long followingId);
  
  long countByFollowerId(Long followerId);
  
  long countByFollowingId(Long followingId);
  
  boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
  
  @Query("select uf.followingId from UserFollow uf where uf.followerId = :followerId")
  List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);
  
  @Query("select uf.followerId from UserFollow uf where uf.followingId = :followingId")
  List<Long> findFollowerIdsByFollowingId(@Param("followingId") Long followingId);
}
