package com.shuyou.auth.controller;

import com.shuyou.auth.dto.*;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.BooklistService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/booklists")
public class BooklistController {
  private final BooklistService booklistService;
  private final UserAuthService userAuthService;

  public BooklistController(BooklistService booklistService, UserAuthService userAuthService) {
    this.booklistService = booklistService;
    this.userAuthService = userAuthService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "12") int pageSize,
                                               @RequestParam(defaultValue = "hall") String scope,
                                               @RequestParam(required = false) String sortBy,
                                               @RequestHeader(value = "Authorization", required = false) String authorization) {
    UserAccount me = userAuthService.optionalUser(authorization).orElse(null);
    return ApiResponse.ok(booklistService.list(page, pageSize, scope, me, sortBy));
  }

  @GetMapping("/{booklistId}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable String booklistId,
                                                 @RequestHeader(value = "Authorization", required = false) String authorization) {
    UserAccount me = userAuthService.optionalUser(authorization).orElse(null);
    return ApiResponse.ok(booklistService.detail(booklistId, me));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Map<String, Object>> create(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @RequestBody BooklistCreateRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(booklistService.create(me.getId(), me.getUsername(), request));
  }

  @PutMapping("/{booklistId}")
  public ApiResponse<Map<String, Object>> update(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String booklistId,
                                                  @RequestBody BooklistUpdateRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(booklistService.update(booklistId, request, me));
  }

  @DeleteMapping("/{booklistId}")
  public ApiResponse<Void> delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                                  @PathVariable String booklistId) {
    UserAccount me = userAuthService.requireUser(authorization);
    booklistService.delete(booklistId, me);
    return ApiResponse.ok();
  }

  @PostMapping("/{booklistId}/comments")
  public ApiResponse<Map<String, Object>> addComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @PathVariable String booklistId,
                                                      @RequestBody BooklistCommentCreateRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(booklistService.addComment(booklistId, me, request));
  }

  @PostMapping("/{booklistId}/like")
  public ApiResponse<Map<String, Object>> toggleLike(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @PathVariable String booklistId) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(booklistService.toggleLike(me.getId(), me.getUsername(), booklistId));
  }

  @PostMapping("/{booklistId}/follow")
  public ApiResponse<Map<String, Object>> toggleFollow(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @PathVariable String booklistId) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(booklistService.toggleFollow(me.getId(), booklistId));
  }

  @GetMapping("/collected")
  public ApiResponse<Map<String, Object>> collected(@RequestHeader(value = "Authorization", required = false) String authorization) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(booklistService.collectedBooklists(me.getId()));
  }

  @PostMapping("/{booklistId}/invite")
  public ApiResponse<Map<String, Object>> invite(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String booklistId,
                                                  @RequestBody BooklistInviteRequest request) {
    userAuthService.requireUser(authorization);
    return ApiResponse.ok(booklistService.invite(booklistId, request.userId(), "书友A"));
  }
}
