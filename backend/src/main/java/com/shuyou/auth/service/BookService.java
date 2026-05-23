package com.shuyou.auth.service;

import com.shuyou.auth.entity.Book;
import com.shuyou.auth.entity.BookReview;
import com.shuyou.auth.entity.BookReviewLike;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.BookRepository;
import com.shuyou.auth.repository.BookReviewLikeRepository;
import com.shuyou.auth.repository.BookReviewRepository;
import com.shuyou.auth.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class BookService {
  private static final Logger log = LoggerFactory.getLogger(BookService.class);
  private static final String DEFAULT_COVER_URL = "/photos/default-cover.jpg";
  private static final List<String> BOOK_CATEGORIES = List.of(
    "文学小说", "历史传记", "计算机编程", "心理学",
    "经济学", "少儿绘本", "科普科幻", "管理学"
  );
  private final BookRepository bookRepository;
  private final BookReviewRepository bookReviewRepository;
  private final BookReviewLikeRepository bookReviewLikeRepository;
  private final UserAccountRepository userAccountRepository;

  public BookService(BookRepository bookRepository,
                     BookReviewRepository bookReviewRepository,
                     BookReviewLikeRepository bookReviewLikeRepository,
                     UserAccountRepository userAccountRepository) {
    this.bookRepository = bookRepository;
    this.bookReviewRepository = bookReviewRepository;
    this.bookReviewLikeRepository = bookReviewLikeRepository;
    this.userAccountRepository = userAccountRepository;
  }

  private Map<String, Object> toDto(Book book) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", book.getCode());
    map.put("title", book.getTitle());
    map.put("author", book.getAuthor());
    map.put("tag", book.getTag());
    map.put("rating", book.getRating() == null ? "0" : String.format(Locale.ROOT, "%.1f", book.getRating()));
    map.put("reviews", book.getReviews() == null ? 0 : book.getReviews());
    map.put("cover", resolveCover(book.getCoverUrl()));
    map.put("description", book.getDescription() == null ? "" : book.getDescription());
    return map;
  }

  private Map<String, Object> reviewToDto(BookReview review, Long currentUserId) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", review.getId());
    map.put("rating", review.getRating());
    map.put("content", review.getContent() == null ? "" : review.getContent());
    map.put("userName", userAccountRepository.findById(review.getUserId()).map(UserAccount::getUsername).orElse("匿名用户"));
    map.put("userId", "u-" + review.getUserId());
    map.put("avatarUrl", userAccountRepository.findById(review.getUserId()).map(u -> u.getAvatarUrl() == null ? "" : u.getAvatarUrl()).orElse(""));
    map.put("createdAt", review.getCreatedAt() != null ? review.getCreatedAt().toString() : Instant.now().toString());
    map.put("likeCount", bookReviewLikeRepository.countByReviewId(review.getId()));
    map.put("liked", currentUserId != null && bookReviewLikeRepository.findByReviewIdAndUserId(review.getId(), currentUserId).isPresent());
    return map;
  }

  public Map<String, Object> likeReview(Long userId, Long reviewId, boolean like) {
    BookReview review = bookReviewRepository.findById(reviewId)
      .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
    
    Optional<BookReviewLike> existing = bookReviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);
    
    if (like) {
      if (existing.isEmpty()) {
        BookReviewLike likeEntity = new BookReviewLike();
        likeEntity.setReviewId(reviewId);
        likeEntity.setUserId(userId);
        bookReviewLikeRepository.save(likeEntity);
      }
    } else {
      existing.ifPresent(bookReviewLikeRepository::delete);
    }
    
    long likeCount = bookReviewLikeRepository.countByReviewId(reviewId);
    return Map.of(
      "reviewId", reviewId,
      "liked", like,
      "likeCount", likeCount
    );
  }

  @Transactional
  public void deleteReview(Long userId, Long reviewId) {
    BookReview review = bookReviewRepository.findById(reviewId)
      .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
    
    if (!Objects.equals(review.getUserId(), userId)) {
      throw new IllegalArgumentException("只能删除自己的评论");
    }
    
    bookReviewLikeRepository.deleteByReviewId(reviewId);
    bookReviewRepository.delete(review);
    
    updateBookRating(review.getBookCode());
  }

  public Map<String, Object> featured(int page, int pageSize) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.ASC, "id"));
    Page<Book> result = bookRepository.findByFeaturedTrue(pageable);
    return pagedData(result, safePage, safePageSize);
  }

  public Map<String, Object> list(int page, int pageSize, String search, String tag) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);
    Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.ASC, "id"));
    Page<Book> result = bookRepository.search(normalize(search), normalize(tag), pageable);
    return pagedData(result, safePage, safePageSize);
  }

  public Map<String, Object> detail(String bookId) {
    log.debug("Fetching book detail: {}", bookId);
    Book book = bookRepository.findByCode(bookId).orElseThrow(() -> new IllegalArgumentException("书籍不存在"));
    Map<String, Object> detail = new LinkedHashMap<>(toDto(book));
    detail.put("publishDate", book.getPublishDate());
    detail.put("publisher", book.getPublisher());
    detail.put("pages", book.getPages());
    detail.put("isbn", book.getIsbn());
    detail.put("longDescription", book.getLongDescription() != null ? book.getLongDescription() : "");
    detail.put("authorIntro", book.getAuthorIntro() != null ? book.getAuthorIntro() : "");
    detail.put("language", book.getLanguage() != null ? book.getLanguage() : "中文");
    detail.put("category", book.getCategory() != null ? book.getCategory() : "");

    List<Map<String, Object>> related = bookRepository.search(null, book.getTag(), PageRequest.of(0, 6))
      .getContent()
      .stream()
      .filter(item -> !Objects.equals(item.getCode(), book.getCode()))
      .limit(3)
      .map(this::toDto)
      .toList();
    detail.put("relatedBooks", related);

    List<BookReview> reviews = bookReviewRepository.findByBookCodeOrderByCreatedAtDesc(bookId);
    detail.put("reviewList", reviews.stream().map(r -> reviewToDto(r, null)).toList());

    return detail;
  }

  @Transactional
  public Map<String, Object> addReview(Long userId, String bookId, Double rating, String content) {
    log.info("User {} adding review for book {}", userId, bookId);
    Book book = bookRepository.findByCode(bookId).orElseThrow(() -> new IllegalArgumentException("书籍不存在"));

    UserAccount user = userAccountRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

    BookReview review = new BookReview();
    review.setBookCode(bookId);
    review.setUserId(userId);
    review.setRating(rating);
    review.setContent(content);
    review.setCreatedAt(Instant.now());
    review.setUpdatedAt(Instant.now());

    bookReviewRepository.save(review);

    updateBookRating(bookId);

    Map<String, Object> result = reviewToDto(review, userId);
    log.info("Review saved and returning: {}", result);
    return result;
  }

  public List<Map<String, Object>> getReviews(String bookId, int page, int pageSize, String sortBy, Long currentUserId) {
    int safePage = Math.max(page, 1);
    int safePageSize = Math.max(pageSize, 1);

    Sort sort = "hot".equalsIgnoreCase(sortBy)
      ? Sort.by(Sort.Direction.DESC, "rating", "createdAt")
      : Sort.by(Sort.Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(safePage - 1, safePageSize, sort);
    Page<BookReview> reviews = bookReviewRepository.findByBookCode(bookId, pageable);
    return reviews.getContent().stream().map(r -> reviewToDto(r, currentUserId)).toList();
  }

  public Map<String, Object> getUserReview(Long userId, String bookId) {
    List<BookReview> reviews = bookReviewRepository.findByBookCodeAndUserId(bookId, userId);
    return reviews.isEmpty() ? null : reviewToDto(reviews.get(0), userId);
  }

  @Transactional
  public void updateBookRating(String bookId) {
    Double avgRating = bookReviewRepository.getAverageRatingByBookCode(bookId);
    Long reviewCount = bookReviewRepository.countByBookCode(bookId);

    bookRepository.findByCode(bookId).ifPresent(book -> {
      book.setRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
      book.setReviews(reviewCount != null ? reviewCount.intValue() : 0);
      bookRepository.save(book);
    });
  }

  private Map<String, Object> pagedData(Page<Book> pageData, int page, int pageSize) {
    List<Map<String, Object>> items = pageData.getContent().stream().map(this::toDto).toList();
    return Map.of(
      "total", pageData.getTotalElements(),
      "page", page,
      "pageSize", pageSize,
      "items", items
    );
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public List<String> getCategories() {
    return BOOK_CATEGORIES;
  }

  private String resolveCover(String coverUrl) {
    if (coverUrl == null || coverUrl.isBlank() || Objects.equals(coverUrl, "image-url")) {
      return DEFAULT_COVER_URL;
    }
    return coverUrl;
  }
}
