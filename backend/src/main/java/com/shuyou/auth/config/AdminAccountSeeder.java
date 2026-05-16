package com.shuyou.auth.config;

import com.shuyou.auth.entity.UserAccount;
import com.shuyou.auth.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountSeeder implements ApplicationRunner {
  private final UserAccountRepository userAccountRepository;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AdminAccountSeeder(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    String adminEmail = "admin@shuyou.com";
    UserAccount admin = userAccountRepository.findByEmail(adminEmail).orElseGet(UserAccount::new);
    admin.setUsername("admin");
    admin.setEmail(adminEmail);
    admin.setPasswordHash(passwordEncoder.encode("admin123456"));
    admin.setTitle("系统管理员");
    admin.setBio("ShuYou 管理后台管理员账号");
    admin.setAvatarUrl("");
    admin.setPhone("");
    admin.setRole("ADMIN");
    admin.setStatus(1);
    userAccountRepository.save(admin);
  }
}
