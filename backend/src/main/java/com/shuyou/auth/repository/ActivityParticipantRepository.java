package com.shuyou.auth.repository;

import com.shuyou.auth.entity.ActivityParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {
  Optional<ActivityParticipant> findByActivityCodeAndUserId(String activityCode, Long userId);
  long countByActivityCode(String activityCode);
  void deleteByActivityCode(String activityCode);
  Page<ActivityParticipant> findByUserId(Long userId, Pageable pageable);
  @Query("SELECT ap.activityCode FROM ActivityParticipant ap WHERE ap.userId = :userId")
  List<String> findActivityCodesByUserId(@Param("userId") Long userId);
}
