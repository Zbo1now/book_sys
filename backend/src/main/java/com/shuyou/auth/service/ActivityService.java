package com.shuyou.auth.service;

import com.shuyou.auth.dto.ActivityCreateRequest;
import com.shuyou.auth.dto.ActivityUpdateRequest;
import com.shuyou.auth.entity.Activity;
import com.shuyou.auth.entity.ActivityParticipant;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.ActivityParticipantRepository;
import com.shuyou.auth.repository.ActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActivityService {
  private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

  private final ActivityRepository activityRepository;
  private final ActivityParticipantRepository participantRepository;

  public ActivityService(ActivityRepository activityRepository, ActivityParticipantRepository participantRepository) {
    this.activityRepository = activityRepository;
    this.participantRepository = participantRepository;
  }

  public Map<String, Object> list(int page, int pageSize, String status, Long userId) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Page<Activity> result;
    PageRequest pageRequest = PageRequest.of(safePage - 1, safePageSize);

    if (status != null && !status.isBlank()) {
      result = activityRepository.findByStatusAndApprovalStatus(status, "approved", PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    } else {
      result = activityRepository.findByApprovalStatus("approved", PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    }

    List<Activity> activities = result.getContent();
    final Set<String> participatingCodes;
    if (userId != null && !activities.isEmpty()) {
      participatingCodes = participantRepository.findActivityCodesByUserId(userId)
        .stream()
        .collect(Collectors.toSet());
    } else {
      participatingCodes = Set.of();
    }

    return Map.of(
      "total", result.getTotalElements(),
      "page", safePage,
      "pageSize", safePageSize,
      "items", activities.stream().map(a -> toDto(a, participatingCodes)).toList()
    );
  }

  public Map<String, Object> listAll(int page, int pageSize, String status, String approvalStatus) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Page<Activity> result;
    if (approvalStatus != null && !approvalStatus.isBlank()) {
      result = activityRepository.findByApprovalStatus(approvalStatus, PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    } else if (status != null && !status.isBlank()) {
      result = activityRepository.findByStatus(status, PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    } else {
      result = activityRepository.findAll(PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    }
    return Map.of(
      "total", result.getTotalElements(),
      "page", safePage,
      "pageSize", safePageSize,
      "items", result.getContent().stream().map(a -> toDto(a, Set.of())).toList()
    );
  }

  public Map<String, Object> detail(String id, Long userId) {
    Activity item = activityRepository.findByCode(id).orElseThrow(() -> new IllegalArgumentException("活动不存在"));
    Set<String> participatingCodes = Set.of();
    if (userId != null) {
      boolean isParticipating = participantRepository.findByActivityCodeAndUserId(id, userId).isPresent();
      if (isParticipating) {
        participatingCodes = Set.of(id);
      }
    }
    return toDto(item, participatingCodes);
  }

  public Map<String, Object> create(Long userId, String username, ActivityCreateRequest request) {
    log.info("Creating activity for user: {}", username);
    Activity item = new Activity();
    item.setCode(generateCode("activity"));
    item.setTitle(defaultIfBlank(request.title(), "未命名活动"));
    item.setDescription(defaultIfBlank(request.description(), ""));
    item.setCoverUrl(defaultIfBlank(request.cover(), ""));
    item.setOrganizerId(userId);
    item.setOrganizerName(username);
    item.setStartDate(request.startDate());
    item.setEndDate(request.endDate());
    item.setLocation(request.location());
    item.setParticipantCount(0);
    item.setMaxParticipants(request.maxParticipants());
    item.setStatus("upcoming");
    item.setActivityType(request.activityType() != null ? request.activityType() : "offline");
    Activity saved = activityRepository.save(item);
    log.info("Activity created with code: {}", saved.getCode());
    return toDto(saved, Set.of());
  }

  public Map<String, Object> participate(Long userId, String activityId, String action) {
    log.info("User {} attempting to {} activity {}", userId, action, activityId);
    Activity item = activityRepository.findByCode(activityId).orElseThrow(() -> new IllegalArgumentException("活动不存在"));

    if ("leave".equalsIgnoreCase(action)) {
      Optional<ActivityParticipant> participant = participantRepository.findByActivityCodeAndUserId(activityId, userId);
      participant.ifPresent(participantRepository::delete);
      int participantCount = (int) participantRepository.countByActivityCode(activityId);
      item.setParticipantCount(participantCount);
      activityRepository.save(item);
      log.info("User {} left activity {}", userId, activityId);
      return Map.of("participating", false, "participantCount", participantCount);
    }

    if (item.getEndDate() != null && !item.getEndDate().isBlank()) {
      Instant deadline = parseDeadline(item.getEndDate());
      if (deadline != null && deadline.isBefore(Instant.now())) {
        throw new IllegalArgumentException("活动已结束，无法报名");
      }
    }

    if (item.getStartDate() != null && !item.getStartDate().isBlank()) {
      Instant startTime = parseDeadline(item.getStartDate());
      if (startTime != null) {
        Instant cutoff = startTime.minusSeconds(3 * 3600);
        if (Instant.now().isAfter(cutoff)) {
          throw new IllegalArgumentException("报名已截止（活动开始前3小时停止报名）");
        }
      }
    }

    if (item.getMaxParticipants() != null && item.getMaxParticipants() > 0) {
      long currentCount = participantRepository.countByActivityCode(activityId);
      if (currentCount >= item.getMaxParticipants()) {
        throw new IllegalArgumentException("活动人数已满");
      }
    }
    Optional<ActivityParticipant> existing = participantRepository.findByActivityCodeAndUserId(activityId, userId);
    if (existing.isEmpty()) {
      ActivityParticipant created = new ActivityParticipant();
      created.setActivityCode(activityId);
      created.setUserId(userId);
      participantRepository.save(created);
    }
    int participantCount = (int) participantRepository.countByActivityCode(activityId);
    item.setParticipantCount(participantCount);
    activityRepository.save(item);
    log.info("User {} joined activity {}. Total participants: {}", userId, activityId, participantCount);
    return Map.of("participating", true, "participantCount", participantCount);
  }

  public Map<String, Object> update(String activityId, ActivityUpdateRequest request, UserAccount actor) {
    Activity item = activityRepository.findByCode(activityId).orElseThrow(() -> new IllegalArgumentException("活动不存在"));
    boolean canManage = actor != null && (item.getOrganizerId().equals(actor.getId()) || "ADMIN".equalsIgnoreCase(actor.getRole()));
    if (!canManage) {
      throw new IllegalArgumentException("无权修改该活动");
    }
    if (request.title() != null && !request.title().isBlank()) {
      item.setTitle(request.title().trim());
    }
    if (request.description() != null) {
      item.setDescription(request.description());
    }
    if (request.cover() != null) {
      item.setCoverUrl(request.cover());
    }
    if (request.startDate() != null) {
      item.setStartDate(request.startDate());
    }
    if (request.endDate() != null) {
      item.setEndDate(request.endDate());
    }
    if (request.location() != null) {
      item.setLocation(request.location());
    }
    if (request.maxParticipants() != null) {
      item.setMaxParticipants(request.maxParticipants());
    }
    if (request.status() != null && !request.status().isBlank()) {
      item.setStatus(request.status().trim());
    }
    if (request.approvalStatus() != null && !request.approvalStatus().isBlank()) {
      item.setApprovalStatus(request.approvalStatus().trim());
    }
    activityRepository.save(item);
    return toDto(item, Set.of());
  }

  public Map<String, Object> approve(String activityId, String approvalStatus) {
    Activity item = activityRepository.findByCode(activityId).orElseThrow(() -> new IllegalArgumentException("活动不存在"));
    item.setApprovalStatus(approvalStatus);
    activityRepository.save(item);
    return toDto(item, Set.of());
  }

  @Transactional
  public void delete(String activityId, UserAccount actor) {
    Activity item = activityRepository.findByCode(activityId).orElseThrow(() -> new IllegalArgumentException("活动不存在"));
    boolean canManage = actor != null && (item.getOrganizerId().equals(actor.getId()) || "ADMIN".equalsIgnoreCase(actor.getRole()));
    if (!canManage) {
      throw new IllegalArgumentException("无权删除该活动");
    }
    participantRepository.deleteByActivityCode(activityId);
    activityRepository.delete(item);
    log.info("Activity {} deleted by user {}", activityId, actor.getUsername());
  }

  public long count() {
    return activityRepository.count();
  }

  public Map<String, Object> myActivities(Long userId, int page, int pageSize, String approvalStatus) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    PageRequest pageRequest = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<Activity> result;
    if (approvalStatus != null && !approvalStatus.isBlank()) {
      result = activityRepository.findByOrganizerIdAndApprovalStatus(userId, approvalStatus, pageRequest);
    } else {
      result = activityRepository.findByOrganizerId(userId, pageRequest);
    }
    return Map.of(
      "total", result.getTotalElements(),
      "page", safePage,
      "pageSize", safePageSize,
      "items", result.getContent().stream().map(a -> toSimpleDto(a)).toList()
    );
  }
  
  private Map<String, Object> toSimpleDto(Activity item) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", item.getId());
    dto.put("code", item.getCode());
    dto.put("title", item.getTitle());
    dto.put("description", item.getDescription());
    dto.put("location", item.getLocation());
    dto.put("maxParticipants", item.getMaxParticipants());
    dto.put("participantCount", item.getParticipantCount());
    dto.put("startDate", item.getStartDate());
    dto.put("endDate", item.getEndDate());
    dto.put("status", item.getStatus());
    dto.put("approvalStatus", item.getApprovalStatus());
    dto.put("organizer", Map.of("id", item.getOrganizerId(), "username", item.getOrganizerName()));
    return dto;
  }

  private Map<String, Object> toDto(Activity item, Set<String> participatingCodes) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", item.getCode());
    dto.put("title", item.getTitle());
    dto.put("description", item.getDescription() == null ? "" : item.getDescription());
    dto.put("cover", item.getCoverUrl() == null ? "" : item.getCoverUrl());
    dto.put("organizer", Map.of("id", "u-" + item.getOrganizerId(), "username", item.getOrganizerName()));
    dto.put("startDate", item.getStartDate() == null ? "" : item.getStartDate());
    dto.put("endDate", item.getEndDate() == null ? "" : item.getEndDate());
    dto.put("location", item.getLocation() == null ? "" : item.getLocation());
    dto.put("participantCount", item.getParticipantCount() == null ? 0 : item.getParticipantCount());
    dto.put("maxParticipants", item.getMaxParticipants() == null ? 0 : item.getMaxParticipants());
    dto.put("status", item.getStatus() == null ? "upcoming" : item.getStatus());
    dto.put("dynamicStatus", calculateDynamicStatus(item.getStartDate(), item.getEndDate()));
    dto.put("approvalStatus", item.getApprovalStatus() == null ? "pending" : item.getApprovalStatus());
    dto.put("activityType", item.getActivityType() == null ? "offline" : item.getActivityType());
    dto.put("schedule", List.of());
    dto.put("createdAt", item.getCreatedAt() == null ? "" : item.getCreatedAt().toString());
    dto.put("updatedAt", item.getUpdatedAt() == null ? "" : item.getUpdatedAt().toString());
    dto.put("isParticipating", participatingCodes.contains(item.getCode()));
    return dto;
  }

  private String calculateDynamicStatus(String startDateStr, String endDateStr) {
    Instant now = Instant.now();
    Instant startDate = parseDeadline(startDateStr);
    Instant endDate = parseDeadline(endDateStr);

    if (startDate == null) {
      return "upcoming";
    }

    if (now.isBefore(startDate)) {
      return "upcoming";
    }

    if (endDate != null && now.isAfter(endDate)) {
      return "ended";
    }

    return "ongoing";
  }

  private String generateCode(String prefix) {
    return prefix + "-" + System.currentTimeMillis();
  }

  private String defaultIfBlank(String value, String defaultValue) {
    return value == null || value.isBlank() ? defaultValue : value;
  }

  private Instant parseDeadline(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) return null;
    try {
      return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();
    } catch (DateTimeParseException e) {
      try {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.systemDefault()).toInstant();
      } catch (DateTimeParseException e2) {
        return null;
      }
    }
  }
}
