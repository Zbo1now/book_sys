package com.shuyou.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path cwd = Paths.get("").toAbsolutePath().normalize();
    // 按优先级从高到低：
    // 1. 项目根目录的 uploads/photos（用户上传的永久存储）
    // 2. resources/static/photos（默认静态资源）
    String uploadsPhotos = cwd.resolve("uploads").resolve("photos").toUri().toString();
    String staticPhotos = "classpath:/static/photos/";

    registry.addResourceHandler("/photos/**")
      .addResourceLocations(uploadsPhotos, staticPhotos);

    String uploadsAvatars = cwd.resolve("uploads").resolve("avatars").toUri().toString();
    registry.addResourceHandler("/avatars/**")
      .addResourceLocations(uploadsAvatars);
  }
}
