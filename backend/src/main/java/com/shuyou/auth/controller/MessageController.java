package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.dto.MessageSendRequest;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.MessageService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
  private final MessageService messageService;
  private final UserAuthService userAuthService;

  public MessageController(MessageService messageService, UserAuthService userAuthService) {
    this.messageService = messageService;
    this.userAuthService = userAuthService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> list(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
    UserAccount user = userAuthService.requireUser(authorization);
    return ApiResponse.ok(messageService.list(page, pageSize, user.getId()));
  }

  @GetMapping("/conversations/{conversationId}")
  public ApiResponse<Map<String, Object>> detail(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                 @PathVariable String conversationId,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int pageSize) {
    UserAccount user = userAuthService.requireUser(authorization);
    return ApiResponse.ok(messageService.detail(conversationId, page, pageSize, user.getId()));
  }

  @PostMapping("/send")
  public ApiResponse<Map<String, Object>> send(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               @RequestBody MessageSendRequest request) {
    UserAccount user = userAuthService.requireUser(authorization);
    return ApiResponse.ok(messageService.send(user, request));
  }

  @PutMapping("/conversations/{conversationId}/read")
  public ApiResponse<Map<String, Object>> readAll(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @PathVariable String conversationId) {
    UserAccount user = userAuthService.requireUser(authorization);
    return ApiResponse.ok(messageService.readAll(conversationId, user.getId()));
  }
}