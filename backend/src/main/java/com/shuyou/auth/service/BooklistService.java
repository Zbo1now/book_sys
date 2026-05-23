package com.shuyou.auth.service;

import com.shuyou.auth.dto.BooklistCreateRequest;
import com.shuyou.auth.dto.BooklistCommentCreateRequest;
import com.shuyou.auth.dto.BooklistUpdateRequest;
import com.shuyou.auth.entity.Book;
import com.shuyou.auth.entity.Booklist;
import com.shuyou.auth.entity.BooklistComment;
import com.shuyou.auth.entity.BooklistFollow;
import com.shuyou.auth.entity.BooklistLike;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.BookRepository;
import com.shuyou.auth.repository.BooklistCommentRepository;
import com.shuyou.auth.repository.BooklistFollowRepository;
import com.shuyou.auth.repository.BooklistLikeRepository;
import com.shuyou.auth.repository.BooklistRepository;
import com.shuyou.auth.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BooklistService {
  private static final String DEFAULT_COVER = "/photos/default-cover.jpg";
  private final BooklistRepository booklistRepository;
  private final BooklistCommentRepository commentRepository;
  private final BooklistFollowRepository followRepository;
  private final BooklistLikeRepository likeRepository;
  private final BookRepository bookRepository;
  private final UserAccountRepository userAccountRepository;

  public BooklistService(BooklistRepository booklistRepository,
                         BooklistCommentRepository commentRepository,
                         BooklistFollowRepository followRepository,
                         BooklistLikeRepository likeRepository,
                         BookRepository bookRepository,
                         UserAccountRepository userAccountRepository) {
    this.booklistRepository = booklistRepository;
    this.commentRepository = commentRepository;
    this.followRepository = followRepository;
    this.likeRepository = likeRepository;
    this.bookRepository = bookRepository;
    this.userAccountRepository = userAccountRepository;
  }

  public Map<String, Object> list(int page, int pageSize, String scope, UserAccount currentUser) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    String normalizedScope = scope == null ? "hall" : scope.trim().toLowerCase(Locale.ROOT);
    Page<Booklist> result;
    if ("personal".equals(normalizedScope)) {
      if (currentUser == null) {
        return Map.of("total", 0, "page", safePage, "pageSize", safePageSize, "items", List.of());
      }
      result = booklistRepository.findByCreatorId(currentUser.getId(),
        PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    } else if ("all".equals(normalizedScope)) {
      result = booklistRepository.search(null,
        PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    } else {
      result = booklistRepository.findByIsPublicTrue(
        PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    List<Booklist> content = new ArrayList<>(result.getContent());
    if ("hall".equals(normalizedScope)) {
      content.sort((a, b) -> {
        long likesA = likeRepository.countByBooklistCode(a.getCode());
        long likesB = likeRepository.countByBooklistCode(b.getCode());
        if (likesB != likesA) {
          return Long.compare(likesB, likesA);
        }
        var ta = a.getCreatedAt();
        var tb = b.getCreatedAt();
        if (ta != null && tb != null) {
          return tb.compareTo(ta);
        }
        return 0;
      });
    }

    return Map.of(
      "total", result.getTotalElements(),
      "page", safePage,
      "pageSize", safePageSize,
      "items", content.stream().map(this::toSummaryDto).toList()
    );
  }

  public Map<String, Object> detail(String id, UserAccount currentUser) {
    Booklist booklist = booklistRepository.findByCode(id).orElseThrow(() -> new IllegalArgumentException("书单不存在"));
    ensureCanView(booklist, currentUser);
    Map<String, Object> detail = new LinkedHashMap<>(toSummaryDto(booklist));
    detail.put("books", resolveBooks(booklist.getBookCodes()));
    detail.put("likedUsers", resolveLikedUsers(booklist.getCode()));
    detail.put("comments", resolveComments(booklist.getCode()));
    detail.put("collaborators", List.of());
    if (currentUser != null) {
      detail.put("isLiked", likeRepository.existsByBooklistCodeAndUserId(id, currentUser.getId()));
      detail.put("isFollowed", followRepository.findByBooklistCodeAndUserId(id, currentUser.getId()).isPresent());
    } else {
      detail.put("isLiked", false);
      detail.put("isFollowed", false);
    }
    return detail;
  }

  public Map<String, Object> create(Long userId, String username, BooklistCreateRequest request) {
    Booklist item = new Booklist();
    item.setCode(generateCode("booklist"));
    item.setTitle(defaultIfBlank(request.title(), "未命名书单"));
    item.setDescription(defaultIfBlank(request.description(), ""));
    item.setCoverUrl(resolveCover(request.cover()));
    item.setCreatorId(userId);
    item.setBookCodes(joinBookCodes(request.bookIds()));
    item.setBookCount(parseBookCodes(item.getBookCodes()).size());
    item.setRating(0.0);
    item.setIsPublic(request.isPublic() == null || request.isPublic());
    booklistRepository.save(item);
    return toSummaryDto(item);
  }

  public Map<String, Object> update(String id, BooklistUpdateRequest request, UserAccount actor) {
    Booklist item = booklistRepository.findByCode(id).orElseThrow(() -> new IllegalArgumentException("书单不存在"));
    ensureCanManage(item, actor);
    if (request.title() != null && !request.title().isBlank()) {
      item.setTitle(request.title().trim());
    }
    if (request.description() != null) {
      item.setDescription(request.description());
    }
    if (request.cover() != null) {
      item.setCoverUrl(resolveCover(request.cover()));
    }
    if (request.isPublic() != null) {
      item.setIsPublic(request.isPublic());
    }
    if (request.bookIds() != null) {
      item.setBookCodes(joinBookCodes(request.bookIds()));
      item.setBookCount(parseBookCodes(item.getBookCodes()).size());
    }
    booklistRepository.save(item);
    return toSummaryDto(item);
  }

  @Transactional
  public void delete(String id, UserAccount actor) {
    Booklist item = booklistRepository.findByCode(id).orElseThrow(() -> new IllegalArgumentException("书单不存在"));
    ensureCanManage(item, actor);
    commentRepository.deleteByBooklistCode(item.getCode());
    followRepository.deleteByBooklistCode(item.getCode());
    likeRepository.deleteByBooklistCode(item.getCode());
    booklistRepository.delete(item);
  }

  public Map<String, Object> addComment(String id, UserAccount actor, BooklistCommentCreateRequest request) {
    Booklist item = booklistRepository.findByCode(id).orElseThrow(() -> new IllegalArgumentException("书单不存在"));
    ensureCanView(item, actor);
    BooklistComment comment = new BooklistComment();
    comment.setBooklistCode(id);
    comment.setUserId(actor.getId());
    comment.setContent(request.content().trim());
    commentRepository.save(comment);
    return Map.of(
      "id", comment.getId() == null ? "" : String.valueOf(comment.getId()),
      "username", actor.getUsername(),
      "content", comment.getContent(),
      "createdAt", comment.getCreatedAt() == null ? "" : comment.getCreatedAt().toString()
    );
  }

  @Transactional
  public Map<String, Object> toggleLike(Long userId, String username, String booklistId) {
    Booklist item = booklistRepository.findByCode(booklistId).orElseThrow(() -> new IllegalArgumentException("书单不存在"));
    Optional<BooklistLike> existing = likeRepository.findByBooklistCodeAndUserId(booklistId, userId);
    boolean liked;
    if (existing.isPresent()) {
      likeRepository.delete(existing.get());
      liked = false;
    } else {
      BooklistLike like = new BooklistLike();
      like.setBooklistCode(booklistId);
      like.setUserId(userId);
      likeRepository.save(like);
      liked = true;
    }
    long likeCount = likeRepository.countByBooklistCode(booklistId);
    return Map.of("liked", liked, "likeCount", likeCount);
  }

  @Transactional
  public Map<String, Object> toggleFollow(Long userId, String booklistId) {
    Booklist item = booklistRepository.findByCode(booklistId).orElseThrow(() -> new IllegalArgumentException("书单不存在"));
    Optional<BooklistFollow> follow = followRepository.findByBooklistCodeAndUserId(booklistId, userId);
    boolean followed;
    if (follow.isPresent()) {
      followRepository.delete(follow.get());
      followed = false;
    } else {
      BooklistFollow created = new BooklistFollow();
      created.setBooklistCode(booklistId);
      created.setUserId(userId);
      followRepository.save(created);
      followed = true;
    }
    long followerCount = followRepository.countByBooklistCode(booklistId);
    return Map.of("followed", followed, "followerCount", followerCount);
  }

  public Map<String, Object> collectedBooklists(Long userId) {
    List<BooklistFollow> follows = followRepository.findByUserId(userId);
    List<Map<String, Object>> items = new ArrayList<>();
    for (BooklistFollow f : follows) {
      booklistRepository.findByCode(f.getBooklistCode()).ifPresent(bl -> {
        if (Boolean.TRUE.equals(bl.getIsPublic()) || Objects.equals(bl.getCreatorId(), userId)) {
          items.add(toSummaryDto(bl));
        }
      });
    }
    return Map.of("total", items.size(), "items", items);
  }

  public Map<String, Object> invite(String booklistId, String userId, String username) {
    if (booklistRepository.findByCode(booklistId).isEmpty()) {
      throw new IllegalArgumentException("书单不存在");
    }
    return Map.of(
      "inviteId", "invite-" + booklistId + "-" + userId,
      "invitedUser", Map.of("id", userId, "username", username)
    );
  }

  public long count() {
    return booklistRepository.count();
  }

  private Map<String, Object> toSummaryDto(Booklist item) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", item.getCode());
    dto.put("title", item.getTitle());
    dto.put("description", defaultIfBlank(item.getDescription(), ""));
    dto.put("cover", resolveCover(item.getCoverUrl()));
    dto.put("creator", Map.of("id", "u-" + item.getCreatorId(), "username", userAccountRepository.findById(item.getCreatorId()).map(UserAccount::getUsername).orElse("未知用户")));
    dto.put("bookCount", item.getBookCount() == null ? 0 : item.getBookCount());
    dto.put("bookIds", parseBookCodes(item.getBookCodes()));
    dto.put("followers", (int) followRepository.countByBooklistCode(item.getCode()));
    dto.put("likes", (int) likeRepository.countByBooklistCode(item.getCode()));
    dto.put("rating", item.getRating() == null ? 0 : item.getRating());
    dto.put("createdAt", item.getCreatedAt() == null ? "" : item.getCreatedAt().toString());
    dto.put("updatedAt", item.getUpdatedAt() == null ? "" : item.getUpdatedAt().toString());
    dto.put("isPublic", item.getIsPublic() == null || item.getIsPublic());
    return dto;
  }

  private List<Map<String, Object>> resolveBooks(String bookCodes) {
    List<String> codes = parseBookCodes(bookCodes);
    if (codes.isEmpty()) {
      return List.of();
    }
    Map<String, Book> codeMap = bookRepository.findAll().stream().collect(Collectors.toMap(Book::getCode, b -> b, (a, b) -> a));
    List<Map<String, Object>> result = new ArrayList<>();
    for (String code : codes) {
      Book book = codeMap.get(code);
      if (book == null) {
        continue;
      }
      result.add(Map.of(
        "id", book.getCode(),
        "title", book.getTitle(),
        "author", book.getAuthor(),
        "cover", book.getCoverUrl() == null || book.getCoverUrl().isBlank() ? DEFAULT_COVER : book.getCoverUrl(),
        "rating", book.getRating() == null ? "0.0" : String.format(Locale.ROOT, "%.1f", book.getRating())
      ));
    }
    return result;
  }

  private List<String> parseBookCodes(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    return Arrays.stream(raw.split(","))
      .map(String::trim)
      .filter(it -> !it.isEmpty())
      .distinct()
      .toList();
  }

  private String joinBookCodes(List<String> bookIds) {
    if (bookIds == null || bookIds.isEmpty()) {
      return "";
    }
    return bookIds.stream().map(String::trim).filter(it -> !it.isBlank()).distinct().collect(Collectors.joining(","));
  }

  private String generateCode(String prefix) {
    return prefix + "-" + System.currentTimeMillis();
  }

  private String defaultIfBlank(String value, String defaultValue) {
    return value == null || value.isBlank() ? defaultValue : value;
  }

  private String resolveCover(String cover) {
    if (cover == null || cover.isBlank()) {
      return DEFAULT_COVER;
    }
    return cover;
  }

  private void ensureCanView(Booklist item, UserAccount actor) {
    if (Boolean.TRUE.equals(item.getIsPublic())) {
      return;
    }
    if (actor == null) {
      throw new ForbiddenException("该书单为私密，暂无查看权限");
    }
    boolean canView = Objects.equals(item.getCreatorId(), actor.getId()) || isAdmin(actor);
    if (!canView) {
      throw new ForbiddenException("该书单为私密，暂无查看权限");
    }
  }

  private void ensureCanManage(Booklist item, UserAccount actor) {
    boolean canManage = Objects.equals(item.getCreatorId(), actor.getId()) || isAdmin(actor);
    if (!canManage) {
      throw new ForbiddenException("无权修改该书单");
    }
  }

  private boolean isAdmin(UserAccount actor) {
    return actor != null && "ADMIN".equalsIgnoreCase(actor.getRole());
  }

  private List<Map<String, Object>> resolveComments(String booklistCode) {
    return commentRepository.findByBooklistCodeOrderByCreatedAtDesc(booklistCode).stream()
      .map(c -> {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", String.valueOf(c.getId()));
        m.put("username", userAccountRepository.findById(c.getUserId()).map(UserAccount::getUsername).orElse("未知用户"));
        m.put("userId", "u-" + c.getUserId());
        m.put("content", c.getContent());
        m.put("createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().toString());
        return m;
      })
      .toList();
  }

  private List<Map<String, Object>> resolveLikedUsers(String booklistCode) {
    List<Long> userIds = likeRepository.findByBooklistCode(booklistCode).stream().map(BooklistLike::getUserId).toList();
    if (userIds.isEmpty()) {
      return List.of();
    }
    return userAccountRepository.findAllById(userIds).stream()
      .map(user -> Map.<String, Object>of("id", "u-" + user.getId(), "username", user.getUsername()))
      .toList();
  }
}
