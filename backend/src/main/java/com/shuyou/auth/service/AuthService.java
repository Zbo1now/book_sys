package com.shuyou.auth.service;

import com.shuyou.auth.dto.AuthResponse;
import com.shuyou.auth.dto.AdminLoginRequest;
import com.shuyou.auth.dto.LoginRequest;
import com.shuyou.auth.dto.RegisterRequest;
import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.UserAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserAccountRepository repository;
  private final CaptchaStore captchaStore;
  private final TokenService tokenService;
  private final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public AuthService(UserAccountRepository repository, CaptchaStore captchaStore, TokenService tokenService) {
    this.repository = repository;
    this.captchaStore = captchaStore;
    this.tokenService = tokenService;
  }

  public AuthResponse register(RegisterRequest request) {
    validateCaptcha(request.captchaId(), request.captchaValue());
    if (repository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("账号已存在");
    }
    if (repository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("邮箱已存在");
    }
    UserAccount account = new UserAccount();
    account.setUsername(request.username());
    account.setEmail(request.email());
    account.setPasswordHash(encoder.encode(request.password()));
    account.setTitle("城市阅读者");
    account.setBio("");
    account.setAvatarUrl("");
    account.setPhone("");
    account.setRole("USER");
    repository.save(account);
    return new AuthResponse(account.getId(), account.getUsername(), account.getEmail(), tokenService.issueToken(account.getId()), account.getRole());
  }

  public AuthResponse login(LoginRequest request) {
    validateCaptcha(request.captchaId(), request.captchaValue());
    UserAccount account = repository.findByUsernameOrEmail(request.account(), request.account())
      .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));
    if (account.getStatus() == null || account.getStatus() != 1) {
      throw new IllegalArgumentException("账号已被禁用");
    }
    if (!encoder.matches(request.password(), account.getPasswordHash())) {
      throw new IllegalArgumentException("账号或密码错误");
    }
    if ("admin".equals(request.role()) && !"ADMIN".equalsIgnoreCase(account.getRole())) {
      throw new IllegalArgumentException("该账号不是管理员");
    }
    return new AuthResponse(account.getId(), account.getUsername(), account.getEmail(), tokenService.issueToken(account.getId()), account.getRole());
  }

  public AuthResponse adminLogin(AdminLoginRequest request) {
    UserAccount account = repository.findByUsernameOrEmail(request.account(), request.account())
      .orElseThrow(() -> new IllegalArgumentException("管理员账号或密码错误"));
    if (account.getStatus() == null || account.getStatus() != 1) {
      throw new IllegalArgumentException("管理员账号已被禁用");
    }
    if (!"ADMIN".equalsIgnoreCase(account.getRole())) {
      throw new ForbiddenException("当前账号无管理员权限");
    }
    if (!encoder.matches(request.password(), account.getPasswordHash())) {
      throw new IllegalArgumentException("管理员账号或密码错误");
    }
    return new AuthResponse(account.getId(), account.getUsername(), account.getEmail(), tokenService.issueToken(account.getId()), account.getRole());
  }

  private void validateCaptcha(String id, String code) {
    if (!captchaStore.verify(id, code)) {
      throw new IllegalArgumentException("验证码错误或已过期");
    }
  }
}
