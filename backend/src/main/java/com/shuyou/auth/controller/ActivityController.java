package com.shuyou.auth.controller;

import com.shuyou.auth.dto.ActionRequest;
import com.shuyou.auth.dto.ActivityCreateRequest;
import com.shuyou.auth.dto.ActivityUpdateRequest;
import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.service.ActivityService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
  private final ActivityService activityService;
  private final UserAuthService userAuthService;

  public ActivityController(ActivityService activityService, UserAuthService userAuthService) {
    this.activityService = activityService;
    this.userAuthService = userAuthService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "12") int pageSize,
                                               @RequestParam(required = false) String status,
                                               @RequestHeader(value = "Authorization", required = false) String authorization) {
    UserAccount me = userAuthService.optionalUser(authorization).orElse(null);
    return ApiResponse.ok(activityService.list(page, pageSize, status, me != null ? me.getId() : null));
  }

  @GetMapping("/my")
  public ApiResponse<Map<String, Object>> myActivities(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "12") int pageSize,
                                                       @RequestParam(required = false) String approvalStatus,
                                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
    java.util.Optional<UserAccount> me = userAuthService.optionalUser(authorization);
    if (me.isEmpty()) {
      return ApiResponse.ok(Map.of(
        "total", 0,
        "page", 1,
        "pageSize", pageSize,
        "items", java.util.List.of()
      ));
    }
    return ApiResponse.ok(activityService.myActivities(me.get().getId(), page, pageSize, approvalStatus));
  }

  @GetMapping("/{activityId}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable String activityId,
                                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
    UserAccount me = userAuthService.optionalUser(authorization).orElse(null);
    return ApiResponse.ok(activityService.detail(activityId, me != null ? me.getId() : null));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Map<String, Object>> create(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @RequestBody ActivityCreateRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(activityService.create(me.getId(), me.getUsername(), request));
  }

  @PutMapping("/{activityId}")
  public ApiResponse<Map<String, Object>> update(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String activityId,
                                                  @RequestBody ActivityUpdateRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(activityService.update(activityId, request, me));
  }

  @DeleteMapping("/{activityId}")
  public ApiResponse<Void> delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                                   @PathVariable String activityId) {
    UserAccount me = userAuthService.requireUser(authorization);
    activityService.delete(activityId, me);
    return ApiResponse.ok();
  }

  @PostMapping("/{activityId}/participate")
  public ApiResponse<Map<String, Object>> participate(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       @PathVariable String activityId,
                                                       @RequestBody ActionRequest request) {
    UserAccount me = userAuthService.requireUser(authorization);
    return ApiResponse.ok(activityService.participate(me.getId(), activityId, request.action()));
  }
}
