package com.shuyou.auth.controller;

import com.shuyou.auth.dto.AdminBookUpdateRequest;
import com.shuyou.auth.dto.ApiResponse;
import com.shuyou.auth.service.AdminService;
import com.shuyou.auth.service.UserAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {
  private final UserAuthService userAuthService;
  private final AdminService adminService;

  public AdminBookController(UserAuthService userAuthService, AdminService adminService) {
    this.userAuthService = userAuthService;
    this.adminService = adminService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> list(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String tag) {
    userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(adminService.listBooks(page, pageSize, keyword, tag));
  }

  @GetMapping("/categories")
  public ApiResponse<List<String>> getCategories(@RequestHeader(value = "Authorization", required = false) String authorization) {
    userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(adminService.getBookCategories());
  }

  @PutMapping("/{bookId}")
  public ApiResponse<Map<String, Object>> update(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String bookId,
                                                  @RequestParam(required = false) String title,
                                                  @RequestParam(required = false) String author,
                                                  @RequestParam(required = false) String tag,
                                                  @RequestParam(required = false) String description,
                                                  @RequestParam(required = false) Boolean featured,
                                                  @RequestPart(required = false) MultipartFile cover) {
    userAuthService.requireAdmin(authorization);
    String coverUrl = cover != null && !cover.isEmpty() ? storeCoverFile(cover) : null;
    AdminBookUpdateRequest request = new AdminBookUpdateRequest(title, author, tag, description, featured, coverUrl);
    return ApiResponse.ok(adminService.updateBook(bookId, request));
  }

  @DeleteMapping("/{bookId}")
  public ApiResponse<Map<String, Object>> delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable String bookId) {
    userAuthService.requireAdmin(authorization);
    return ApiResponse.ok(adminService.deleteBook(bookId));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @RequestParam String title,
                                                  @RequestParam String author,
                                                  @RequestParam(required = false) String tag,
                                                  @RequestParam(required = false) String description,
                                                  @RequestParam(required = false) Boolean featured,
                                                  @RequestParam(required = false) String publishDate,
                                                  @RequestParam(required = false) String publisher,
                                                  @RequestParam(required = false) Integer pages,
                                                  @RequestParam(required = false) String isbn,
                                                  @RequestPart(required = false) MultipartFile cover) {
    userAuthService.requireAdmin(authorization);
    String coverUrl = storeCoverFile(cover);
    return ApiResponse.ok(adminService.createBook(title, author, tag, description, featured, coverUrl, publishDate, publisher, pages, isbn));
  }

  private String storeCoverFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return "";
    }
    String original = file.getOriginalFilename() == null ? "cover.jpg" : file.getOriginalFilename();
    String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT) : ".jpg";
    String fileName = "book-cover-" + System.currentTimeMillis() + extension;
    try {
      Path cwd = Paths.get("").toAbsolutePath().normalize();
      // 保存到项目根目录的 uploads/photos 文件夹（持久化存储）
      Path uploadsDir = cwd.resolve("uploads").resolve("photos").normalize();
      Files.createDirectories(uploadsDir);
      Path target = uploadsDir.resolve(fileName);
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
      return "/photos/" + fileName;
    } catch (IOException ex) {
      throw new IllegalArgumentException("封面上传失败: " + ex.getMessage());
    }
  }
}
