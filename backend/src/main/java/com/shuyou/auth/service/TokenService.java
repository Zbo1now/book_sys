package com.shuyou.auth.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
  private final SecureRandom secureRandom = new SecureRandom();
  private final Map<String, Long> tokenToUser = new ConcurrentHashMap<>();

  public String issueToken(Long userId) {
    byte[] bytes = new byte[24];
    secureRandom.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    tokenToUser.put(token, userId);
    return token;
  }

  public Optional<Long> parseUserId(String authorizationHeader) {
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      return Optional.empty();
    }
    if (!authorizationHeader.startsWith("Bearer ")) {
      return Optional.empty();
    }
    String token = authorizationHeader.substring("Bearer ".length()).trim();
    if (token.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(tokenToUser.get(token));
  }
}
