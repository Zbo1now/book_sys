package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.dto.BookReviewRequest;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.BookService;
import com.shuyou.auth.service.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {
  private final BookService bookService;
  private final UserAuthService userAuthService;

  public BookController(BookService bookService, UserAuthService userAuthService) {
    this.bookService = bookService;
    this.userAuthService = userAuthService;
  }

  @GetMapping("/categories")
  public ApiResponse<List<String>> getCategories() {
    return ApiResponse.ok(bookService.getCategories());
  }

  @GetMapping("/featured")
  public ApiResponse<Map<String, Object>> featured(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
    return ApiResponse.ok(bookService.featured(page, pageSize));
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int pageSize,
                                               @RequestParam(required = false) String search,
                                               @RequestParam(required = false) String tag) {
    return ApiResponse.ok(bookService.list(page, pageSize, search, tag));
  }

  @GetMapping("/{bookId}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable String bookId) {
    return ApiResponse.ok(bookService.detail(bookId));
  }

  @PostMapping("/{bookId}/reviews")
  public ApiResponse<Map<String, Object>> addReview(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                     @PathVariable String bookId,
                                                     @Valid @RequestBody BookReviewRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(bookService.addReview(me.getId(), bookId, request.rating(), request.content()));
  }

  @GetMapping("/{bookId}/reviews")
  public ApiResponse<List<Map<String, Object>>> getReviews(@PathVariable String bookId,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                            @RequestParam(required = false) String sortBy) {
    return ApiResponse.ok(bookService.getReviews(bookId, page, pageSize, sortBy));
  }

  @PostMapping("/{bookId}/reviews/{reviewId}/like")
  public ApiResponse<Map<String, Object>> likeReview(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @PathVariable String bookId,
                                                      @PathVariable Long reviewId) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(bookService.likeReview(me.getId(), reviewId, true));
  }

  @DeleteMapping("/{bookId}/reviews/{reviewId}/like")
  public ApiResponse<Map<String, Object>> unlikeReview(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @PathVariable String bookId,
                                                        @PathVariable Long reviewId) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(bookService.likeReview(me.getId(), reviewId, false));
  }

  @DeleteMapping("/{bookId}/reviews/{reviewId}")
  public ApiResponse<Void> deleteReview(@RequestHeader(value = "Authorization", required = false) String authorization,
                                         @PathVariable String bookId,
                                         @PathVariable Long reviewId) {
    UserAccount me = userAuthService.requireUser(authorization);
    bookService.deleteReview(me.getId(), reviewId);
    return ApiResponse.ok(null);
  }

  @GetMapping("/{bookId}/my-review")
  public ApiResponse<Map<String, Object>> getMyReview(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       @PathVariable String bookId) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(bookService.getUserReview(me.getId(), bookId));
  }
}
