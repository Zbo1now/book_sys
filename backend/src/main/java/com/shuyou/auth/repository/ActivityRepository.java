package com.shuyou.auth.repository;

import com.shuyou.auth.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
  Optional<Activity> findByCode(String code);
  Page<Activity> findByStatus(String status, Pageable pageable);
  Page<Activity> findByApprovalStatus(String approvalStatus, Pageable pageable);
  Page<Activity> findByStatusAndApprovalStatus(String status, String approvalStatus, Pageable pageable);
  Page<Activity> findByOrganizerId(Long organizerId, Pageable pageable);
  Page<Activity> findByOrganizerIdAndApprovalStatus(Long organizerId, String approvalStatus, Pageable pageable);
  @Query("SELECT a FROM Activity a ORDER BY CASE a.status WHEN 'ongoing' THEN 1 WHEN 'upcoming' THEN 2 WHEN 'ended' THEN 3 ELSE 4 END, a.updatedAt DESC")
  Page<Activity> findAllWithCustomSort(Pageable pageable);
}
