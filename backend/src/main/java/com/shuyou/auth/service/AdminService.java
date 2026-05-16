package com.shuyou.auth.service;

import com.shuyou.auth.dto.AdminUserUpdateRequest;
import com.shuyou.auth.dto.AdminBookUpdateRequest;
import com.shuyou.auth.entity.Book;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.BookRepository;
import com.shuyou.auth.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
  private static final List<String> BOOK_CATEGORIES = List.of(
    "文学小说",
    "历史传记",
    "计算机编程",
    "心理学",
    "经济学",
    "少儿绘本",
    "科普科幻",
    "管理学"
  );

  private final UserAccountRepository userAccountRepository;
  private final BookRepository bookRepository;
  private final BooklistService booklistService;
  private final ActivityService activityService;

  public AdminService(UserAccountRepository userAccountRepository,
                      BookRepository bookRepository,
                      BooklistService booklistService,
                      ActivityService activityService) {
    this.userAccountRepository = userAccountRepository;
    this.bookRepository = bookRepository;
    this.booklistService = booklistService;
    this.activityService = activityService;
  }

  public List<String> getBookCategories() {
    return BOOK_CATEGORIES;
  }

  public Map<String, Object> dashboard() {
    long userCount = userAccountRepository.count();
    long activeUserCount = userAccountRepository.search(null, 1, PageRequest.of(0, 1)).getTotalElements();
    long adminCount = userAccountRepository.search(null, null, PageRequest.of(0, 2000))
      .stream()
      .filter(user -> "ADMIN".equalsIgnoreCase(user.getRole()))
      .count();
    long bookCount = bookRepository.count();
    long booklistCount = booklistService.count();
    long activityCount = activityService.count();

    List<Map<String, Object>> recentUsers = userAccountRepository.findAll(PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt")))
      .stream()
      .map(this::toAdminUserDto)
      .toList();

    return Map.of(
      "metrics", Map.of(
        "userCount", userCount,
        "activeUserCount", activeUserCount,
        "adminCount", adminCount,
        "bookCount", bookCount,
        "booklistCount", booklistCount,
        "activityCount", activityCount
      ),
      "recentUsers", recentUsers
    );
  }

  public Map<String, Object> listUsers(int page, int pageSize, String keyword, Integer status) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Page<UserAccount> result = userAccountRepository.search(normalize(keyword), status,
      PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.ASC, "id")));
    return Map.of(
      "total", result.getTotalElements(),
      "page", safePage,
      "pageSize", safePageSize,
      "items", result.getContent().stream().map(this::toAdminUserDto).toList()
    );
  }

  public Map<String, Object> listBooks(int page, int pageSize, String keyword, String tag) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Page<Book> result = bookRepository.search(normalize(keyword), normalize(tag),
      PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.ASC, "id")));
    return Map.of(
      "total", result.getTotalElements(),
      "page", safePage,
      "pageSize", safePageSize,
      "items", result.getContent().stream().map(this::toAdminBookDto).toList()
    );
  }

  public Map<String, Object> updateBook(String bookId, AdminBookUpdateRequest request) {
    Book book = bookRepository.findByCode(bookId).orElseThrow(() -> new IllegalArgumentException("图书不存在"));
    if (request.title() != null && !request.title().isBlank()) {
      book.setTitle(request.title().trim());
    }
    if (request.author() != null && !request.author().isBlank()) {
      book.setAuthor(request.author().trim());
    }
    if (request.tag() != null && !request.tag().isBlank()) {
      book.setTag(normalizeCategory(request.tag()));
    }
    if (request.description() != null) {
      book.setDescription(request.description());
    }
    if (request.featured() != null) {
      book.setFeatured(request.featured());
    }
    if (request.coverUrl() != null) {
      book.setCoverUrl(request.coverUrl());
    }
    bookRepository.save(book);
    return toAdminBookDto(book);
  }

  public Map<String, Object> deleteBook(String bookId) {
    Book book = bookRepository.findByCode(bookId).orElseThrow(() -> new IllegalArgumentException("图书不存在"));
    bookRepository.delete(book);
    return Map.of("id", bookId, "deleted", true);
  }

  public Map<String, Object> createBook(String title,
                                        String author,
                                        String tag,
                                        String description,
                                        Boolean featured,
                                        String coverUrl,
                                        String publishDate,
                                        String publisher,
                                        Integer pages,
                                        String isbn) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("书名不能为空");
    }
    if (author == null || author.isBlank()) {
      throw new IllegalArgumentException("作者不能为空");
    }
    Book book = new Book();
    book.setCode(generateBookCode());
    book.setTitle(title.trim());
    book.setAuthor(author.trim());
    book.setTag(normalizeCategory(tag));
    book.setDescription(description == null ? "" : description);
    book.setFeatured(Boolean.TRUE.equals(featured));
    book.setCoverUrl(coverUrl == null ? "" : coverUrl);
    book.setPublishDate(publishDate);
    book.setPublisher(publisher);
    book.setPages(pages);
    book.setIsbn(isbn);
    book.setReviews(0);
    book.setRating(0.0);
    bookRepository.save(book);
    return toAdminBookDto(book);
  }

  public Map<String, Object> updateUser(Long userId, AdminUserUpdateRequest request) {
    UserAccount user = userAccountRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));

    if (request.username() != null && !request.username().isBlank() && !request.username().equals(user.getUsername())) {
      if (userAccountRepository.existsByUsername(request.username())) {
        throw new IllegalArgumentException("用户名已存在");
      }
      user.setUsername(request.username().trim());
    }

    if (request.email() != null && !request.email().isBlank() && !request.email().equals(user.getEmail())) {
      if (userAccountRepository.existsByEmail(request.email())) {
        throw new IllegalArgumentException("邮箱已存在");
      }
      user.setEmail(request.email().trim());
    }

    if (request.title() != null) {
      user.setTitle(request.title());
    }
    if (request.bio() != null) {
      user.setBio(request.bio());
    }
    if (request.phone() != null) {
      user.setPhone(request.phone());
    }
    if (request.role() != null && !request.role().isBlank()) {
      String role = request.role().trim().toUpperCase();
      if (!"USER".equals(role) && !"ADMIN".equals(role)) {
        throw new IllegalArgumentException("角色只能是 USER 或 ADMIN");
      }
      user.setRole(role);
    }

    userAccountRepository.save(user);
    return toAdminUserDto(user);
  }

  public Map<String, Object> updateUserStatus(Long userId, int status) {
    UserAccount user = userAccountRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    user.setStatus(status == 1 ? 1 : 0);
    userAccountRepository.save(user);
    return Map.of(
      "id", "u-" + user.getId(),
      "status", user.getStatus()
    );
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private Map<String, Object> toAdminUserDto(UserAccount user) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", "u-" + user.getId());
    item.put("username", user.getUsername());
    item.put("email", user.getEmail());
    item.put("title", user.getTitle() == null ? "" : user.getTitle());
    item.put("bio", user.getBio() == null ? "" : user.getBio());
    item.put("phone", user.getPhone() == null ? "" : user.getPhone());
    String normalizedRole = (user.getRole() == null || user.getRole().isBlank()) ? "USER" : user.getRole();
    item.put("role", normalizedRole);
    item.put("status", user.getStatus() == null ? 1 : user.getStatus());
    item.put("createdAt", user.getCreatedAt() == null ? "" : user.getCreatedAt().toString());
    item.put("updatedAt", user.getUpdatedAt() == null ? "" : user.getUpdatedAt().toString());
    return item;
  }

  private Map<String, Object> toAdminBookDto(Book book) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", book.getCode());
    item.put("title", book.getTitle());
    item.put("author", book.getAuthor());
    item.put("tag", book.getTag() == null ? "" : book.getTag());
    item.put("description", book.getDescription() == null ? "" : book.getDescription());
    item.put("featured", Boolean.TRUE.equals(book.getFeatured()));
    item.put("rating", book.getRating() == null ? "0.0" : String.format(java.util.Locale.ROOT, "%.1f", book.getRating()));
    item.put("reviews", book.getReviews() == null ? 0 : book.getReviews());
    item.put("cover", book.getCoverUrl() == null ? "" : book.getCoverUrl());
    return item;
  }

  private String generateBookCode() {
    return "book-" + System.currentTimeMillis();
  }

  private String normalizeCategory(String value) {
    String normalized = value == null ? "" : value.trim();
    if (normalized.isBlank()) {
      return BOOK_CATEGORIES.get(0);
    }
    if (!BOOK_CATEGORIES.contains(normalized)) {
      throw new IllegalArgumentException("图书分类必须为八大类之一");
    }
    return normalized;
  }
}
