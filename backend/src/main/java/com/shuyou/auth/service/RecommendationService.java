package com.shuyou.auth.service;

import com.shuyou.auth.entity.Book;
import com.shuyou.auth.entity.BookReview;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.BookRepository;
import com.shuyou.auth.repository.BookReviewRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
  private final BookReviewRepository bookReviewRepository;
  private final BookRepository bookRepository;

  public RecommendationService(BookReviewRepository bookReviewRepository, BookRepository bookRepository) {
    this.bookReviewRepository = bookReviewRepository;
    this.bookRepository = bookRepository;
  }

  public Map<String, Object> getRecommendations(Long userId, int limit) {
    List<Book> allBooks = bookRepository.findAll();
    
    List<BookReview> userReviews = bookReviewRepository.findByUserId(userId);
    
    List<String> readBookCodes = userReviews.stream()
      .map(BookReview::getBookCode)
      .collect(Collectors.toList());
    
    Map<String, Integer> categoryPreference = buildUserPreference(userReviews);
    
    List<Map<String, Object>> recommendations;
    String reason;
    
    if (categoryPreference.isEmpty()) {
      recommendations = getPopularBooks(allBooks, readBookCodes, limit);
      reason = "为您推荐平台热门图书";
    } else {
      recommendations = getPersonalizedBooks(allBooks, readBookCodes, categoryPreference, limit);
      String topCategory = categoryPreference.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("文学");
      reason = String.format("因您喜欢「%s」类图书，为您推荐以下作品", topCategory);
    }
    
    return Map.of(
      "items", recommendations,
      "reason", reason,
      "total", recommendations.size()
    );
  }

  private Map<String, Integer> buildUserPreference(List<BookReview> reviews) {
    Map<String, Integer> preference = new HashMap<>();
    
    for (BookReview review : reviews) {
      if (review.getRating() != null && review.getRating() >= 4.0) {
        bookRepository.findByCode(review.getBookCode()).ifPresent(book -> {
          String category = book.getTag();
          if (category != null && !category.isBlank()) {
            preference.merge(category, 1, Integer::sum);
          }
        });
      }
    }
    
    return preference;
  }

  private List<Map<String, Object>> getPersonalizedBooks(
    List<Book> allBooks, 
    List<String> readBookCodes,
    Map<String, Integer> categoryPreference,
    int limit
  ) {
    return allBooks.stream()
      .filter(book -> !readBookCodes.contains(book.getCode()))
      .filter(book -> book.getTag() != null && categoryPreference.containsKey(book.getTag()))
      .sorted((a, b) -> {
        int scoreA = categoryPreference.getOrDefault(a.getTag(), 0);
        int scoreB = categoryPreference.getOrDefault(b.getTag(), 0);
        int categoryCompare = Integer.compare(scoreB, scoreA);
        if (categoryCompare != 0) return categoryCompare;
        
        double ratingA = a.getRating() != null ? a.getRating() : 0.0;
        double ratingB = b.getRating() != null ? b.getRating() : 0.0;
        return Double.compare(ratingB, ratingA);
      })
      .limit(limit)
      .map(this::toBookDto)
      .collect(Collectors.toList());
  }

  private List<Map<String, Object>> getPopularBooks(
    List<Book> allBooks,
    List<String> readBookCodes,
    int limit
  ) {
    return allBooks.stream()
      .filter(book -> !readBookCodes.contains(book.getCode()))
      .sorted((a, b) -> {
        double ratingA = a.getRating() != null ? a.getRating() : 0.0;
        double ratingB = b.getRating() != null ? b.getRating() : 0.0;
        return Double.compare(ratingB, ratingA);
      })
      .limit(limit)
      .map(this::toBookDto)
      .collect(Collectors.toList());
  }

  private Map<String, Object> toBookDto(Book book) {
    return Map.<String, Object>of(
      "id", book.getCode(),
      "title", book.getTitle() != null ? book.getTitle() : "",
      "author", book.getAuthor() != null ? book.getAuthor() : "",
      "cover", book.getCoverUrl() != null && !book.getCoverUrl().isBlank() 
        ? book.getCoverUrl() : "/photos/default-cover.jpg",
      "rating", book.getRating() != null ? String.format("%.1f", book.getRating()) : "0.0",
      "tag", book.getTag() != null ? book.getTag() : "",
      "reviews", book.getReviews() != null ? book.getReviews() : 0
    );
  }
}
