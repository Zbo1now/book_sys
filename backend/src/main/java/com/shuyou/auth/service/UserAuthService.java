package com.shuyou.auth.service;

import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAuthService {
  private final TokenService tokenService;
  private final UserAccountRepository userAccountRepository;

  public UserAuthService(TokenService tokenService, UserAccountRepository userAccountRepository) {
    this.tokenService = tokenService;
    this.userAccountRepository = userAccountRepository;
  }

  public UserAccount requireUser(String authorizationHeader) {
    Long userId = tokenService.parseUserId(authorizationHeader)
      .orElseThrow(() -> new IllegalArgumentException("未授权，请先登录"));
    UserAccount user = userAccountRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    if (user.getStatus() == null || user.getStatus() != 1) {
      throw new ForbiddenException("账号已被禁用");
    }
    return user;
  }

  public UserAccount requireAdmin(String authorizationHeader) {
    UserAccount admin = requireUser(authorizationHeader);
    if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
      throw new ForbiddenException("无管理员权限");
    }
    return admin;
  }

  public Optional<UserAccount> optionalUser(String authorizationHeader) {
    Optional<Long> userId = tokenService.parseUserId(authorizationHeader);
    if (userId.isEmpty()) {
      return Optional.empty();
    }
    return userAccountRepository.findById(userId.get());
  }
}
