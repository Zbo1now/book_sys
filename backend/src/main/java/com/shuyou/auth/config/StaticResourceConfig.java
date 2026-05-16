package com.shuyou.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

  @Value("${app.upload.base-dir:./uploads}")
  private String uploadBaseDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path base = Paths.get(uploadBaseDir).toAbsolutePath().normalize();

    String uploadsPhotos = base.resolve("photos").toUri().toString();
    String staticPhotos = "classpath:/static/photos/";
    registry.addResourceHandler("/photos/**")
      .addResourceLocations(uploadsPhotos, staticPhotos);

    String uploadsAvatars = base.resolve("avatars").toUri().toString();
    registry.addResourceHandler("/avatars/**")
      .addResourceLocations(uploadsAvatars);
  }
}
