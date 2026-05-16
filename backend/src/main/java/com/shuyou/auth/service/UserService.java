package com.shuyou.auth.service;

import com.shuyou.auth.dto.ProfileUpdateRequest;
import com.shuyou.auth.entity.Activity;
import com.shuyou.auth.entity.ActivityParticipant;
import com.shuyou.auth.entity.BookReview;
import com.shuyou.auth.entity.Booklist;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserService {
  private final UserAccountRepository userAccountRepository;
  private final BookReviewRepository bookReviewRepository;
  private final BooklistRepository booklistRepository;
  private final ActivityParticipantRepository activityParticipantRepository;
  private final ActivityRepository activityRepository;
  private final BookRepository bookRepository;
  private final Map<Long, Set<Long>> followMap = new ConcurrentHashMap<>();

  public UserService(UserAccountRepository userAccountRepository,
                     BookReviewRepository bookReviewRepository,
                     BooklistRepository booklistRepository,
                     ActivityParticipantRepository activityParticipantRepository,
                     ActivityRepository activityRepository,
                     BookRepository bookRepository) {
    this.userAccountRepository = userAccountRepository;
    this.bookReviewRepository = bookReviewRepository;
    this.booklistRepository = booklistRepository;
    this.activityParticipantRepository = activityParticipantRepository;
    this.activityRepository = activityRepository;
    this.bookRepository = bookRepository;
  }

  public Map<String, Object> getUser(Long userId) {
    UserAccount user = userAccountRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    return publicProfile(user);
  }

  public Map<String, Object> me(UserAccount current) {
    Map<String, Object> data = new LinkedHashMap<>(publicProfile(current));
    data.put("email", current.getEmail());
    data.put("phone", current.getPhone());
    Map<String, Object> stats = new LinkedHashMap<>((Map<String, Object>) data.get("stats"));
    stats.put("collectedBooks", 0);
    data.put("stats", stats);
    return data;
  }

  public Map<String, Object> updateProfile(UserAccount current, ProfileUpdateRequest request) {
    if (request.username() != null && !request.username().isBlank() && !request.username().equals(current.getUsername())) {
      if (userAccountRepository.existsByUsername(request.username())) {
        throw new IllegalArgumentException("用户名已存在");
      }
      current.setUsername(request.username());
    }
    if (request.title() != null) {
      current.setTitle(request.title());
    }
    if (request.bio() != null) {
      current.setBio(request.bio());
    }
    if (request.avatar() != null) {
      current.setAvatarUrl(request.avatar());
    }
    userAccountRepository.save(current);
    return Map.of(
      "id", "u-" + current.getId(),
      "username", current.getUsername(),
      "title", current.getTitle(),
      "updatedAt", current.getUpdatedAt() == null ? "" : current.getUpdatedAt().toString()
    );
  }

  public Map<String, Object> follow(UserAccount me, Long targetUserId, String action) {
    if (Objects.equals(me.getId(), targetUserId)) {
      throw new IllegalArgumentException("不能关注自己");
    }
    userAccountRepository.findById(targetUserId).orElseThrow(() -> new IllegalArgumentException("目标用户不存在"));
    Set<Long> following = followMap.computeIfAbsent(me.getId(), k -> ConcurrentHashMap.newKeySet());
    boolean isFollowing;
    if ("unfollow".equalsIgnoreCase(action)) {
      following.remove(targetUserId);
      isFollowing = false;
    } else {
      following.add(targetUserId);
      isFollowing = true;
    }
    long followerCount = followMap.values().stream().filter(set -> set.contains(targetUserId)).count();
    return Map.of("following", isFollowing, "followerCount", followerCount);
  }

  public Map<String, Object> userBooklists(Long userId, int page, int pageSize) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Page<Booklist> result = booklistRepository.findByCreatorId(userId, PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    List<Map<String, Object>> items = result.getContent().stream().map(bl -> {
      Map<String, Object> map = new HashMap<>();
      map.put("id", bl.getCode());
      map.put("title", bl.getTitle());
      map.put("description", bl.getDescription() == null ? "" : bl.getDescription());
      map.put("cover", bl.getCoverUrl() == null ? "" : bl.getCoverUrl());
      map.put("isPublic", bl.getIsPublic());
      map.put("bookCount", bl.getBookCount());
      map.put("followers", bl.getFollowerCount());
      map.put("rating", bl.getRating());
      return map;
    }).collect(Collectors.toList());
    return Map.of("total", result.getTotalElements(), "page", safePage, "pageSize", safePageSize, "items", items);
  }

  public Map<String, Object> activity(Long userId, int page, int pageSize) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);

    List<Map<String, Object>> items = new ArrayList<>();

    // 1. 评论记录
    List<BookReview> reviews = bookReviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    for (BookReview r : reviews) {
      String bookTitle = bookRepository.findByCode(r.getBookCode())
        .map(b -> b.getTitle()).orElse(r.getBookCode());
      String contentPreview = r.getContent() != null && !r.getContent().isBlank() 
        ? (r.getContent().length() > 30 ? r.getContent().substring(0, 30) + "..." : r.getContent())
        : "";
      String title = contentPreview.isEmpty() 
        ? String.format("评分《%s》%.1f", bookTitle, r.getRating())
        : String.format("评论《%s》%s", bookTitle, contentPreview);
      items.add(Map.of(
        "type", "review",
        "title", title,
        "meta", formatTimeAgo(r.getCreatedAt()) + " · 评论记录",
        "timestamp", r.getCreatedAt() != null ? r.getCreatedAt() : Instant.now()
      ));
    }

    // 2. 书单创建记录
    Page<Booklist> booklists = booklistRepository.findByCreatorId(userId, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt")));
    for (Booklist bl : booklists.getContent()) {
      items.add(Map.of(
        "type", "booklist",
        "title", String.format("创建《%s》", bl.getTitle()),
        "meta", formatTimeAgo(bl.getCreatedAt()) + " · 书单记录",
        "timestamp", bl.getCreatedAt()
      ));
    }

    // 3. 活动报名记录
    Page<ActivityParticipant> participations = activityParticipantRepository.findByUserId(userId, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")));
    for (ActivityParticipant ap : participations.getContent()) {
      Optional<Activity> actOpt = activityRepository.findByCode(ap.getActivityCode());
      String actTitle = actOpt.map(a -> a.getTitle()).orElse("未知活动");
      Instant actTime = actOpt.map(a -> a.getCreatedAt()).orElse(null);
      items.add(Map.of(
        "type", "activity",
        "title", String.format("报名《%s》", actTitle),
        "meta", actTime != null ? (formatTimeAgo(actTime) + " · 活动记录") : "活动记录",
        "timestamp", actTime != null ? actTime : Instant.EPOCH
      ));
    }

    // 按真实时间戳倒序（最近的时间排在前面）
    items.sort((a, b) -> {
      Instant tsA = (Instant) a.get("timestamp");
      Instant tsB = (Instant) b.get("timestamp");
      return tsB.compareTo(tsA);
    });

    int from = (safePage - 1) * safePageSize;
    int to = Math.min(from + safePageSize, items.size());
    List<Map<String, Object>> pageItems = from < items.size() ? items.subList(from, to) : List.of();

    // 返回前移除内部字段
    List<Map<String, Object>> resultItems = pageItems.stream().map(m -> {
      Map<String, Object> copy = new LinkedHashMap<>(m);
      copy.remove("timestamp");
      return copy;
    }).toList();

    return Map.of("total", items.size(), "page", safePage, "pageSize", safePageSize, "items", resultItems);
  }

  private String formatTimeAgo(Instant instant) {
    if (instant == null) return "未知时间";
    long seconds = java.time.Duration.between(instant, Instant.now()).getSeconds();
    if (seconds < 60) return "刚刚";
    if (seconds < 3600) return (seconds / 60) + "分钟前";
    if (seconds < 86400) return (seconds / 3600) + "小时前";
    if (seconds < 86400 * 30) return (seconds / 86400) + "天前";
    if (seconds < 86400 * 365) return (seconds / (86400 * 30)) + "个月前";
    return (seconds / (86400 * 365)) + "年前";
  }

  public Map<String, Object> following(Long userId, int page, int pageSize) {
    List<Map<String, Object>> items = List.of(Map.of(
      "id", "u-2",
      "username", "书友A",
      "avatar", "avatar-url",
      "title", "资深书迷",
      "isFollowing", true
    ));
    return Map.of("total", items.size(), "page", page, "pageSize", pageSize, "items", items);
  }

  public Map<String, Object> followers(Long userId, int page, int pageSize) {
    List<Map<String, Object>> items = List.of(Map.of(
      "id", "u-100",
      "username", "新粉丝",
      "avatar", "avatar-url",
      "title", "爱好读书的设计师"
    ));
    return Map.of("total", items.size(), "page", page, "pageSize", pageSize, "items", items);
  }

  public Map<String, Object> searchUsers(String keyword, int page, int pageSize, Long excludeUserId) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Page<UserAccount> result = userAccountRepository.search(keyword, null, PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.ASC, "username")));
    
    List<Map<String, Object>> items = result.getContent().stream()
      .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
      .map(u -> {
        Map<String, Object> userMap = new java.util.HashMap<>();
        userMap.put("id", "u-" + u.getId());
        userMap.put("username", u.getUsername());
        userMap.put("avatar", u.getAvatarUrl() == null ? "" : u.getAvatarUrl());
        userMap.put("title", u.getTitle() == null ? "" : u.getTitle());
        return userMap;
      })
      .collect(Collectors.toList());
    
    Map<String, Object> resultMap = new java.util.HashMap<>();
    resultMap.put("total", result.getTotalElements());
    resultMap.put("page", safePage);
    resultMap.put("pageSize", safePageSize);
    resultMap.put("items", items);
    return resultMap;
  }

  private Map<String, Object> publicProfile(UserAccount user) {
    long followingCount = followMap.getOrDefault(user.getId(), Set.of()).size();
    long followersCount = followMap.values().stream().filter(set -> set.contains(user.getId())).count();
    return Map.of(
      "id", "u-" + user.getId(),
      "username", user.getUsername(),
      "avatar", user.getAvatarUrl() == null ? "" : user.getAvatarUrl(),
      "title", user.getTitle() == null ? "城市阅读者" : user.getTitle(),
      "bio", user.getBio() == null ? "" : user.getBio(),
      "stats", Map.of("following", followingCount, "followers", followersCount, "booklists", 0),
      "joinDate", user.getCreatedAt() == null ? "" : user.getCreatedAt().toString()
    );
  }
}
