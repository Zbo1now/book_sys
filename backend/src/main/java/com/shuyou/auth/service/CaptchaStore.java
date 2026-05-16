package com.shuyou.auth.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Component
public class CaptchaStore {
  private record CaptchaValue(String code, Instant expireAt) {}

  private final Map<String, CaptchaValue> store = new ConcurrentHashMap<>();
  private final Random random = new Random();

  public CaptchaResult generate() {
    String id = UUID.randomUUID().toString();
    String code = randomCode();
    store.put(id, new CaptchaValue(code, Instant.now().plusSeconds(300)));
    String imageBase64 = generateImage(code);
    return new CaptchaResult(id, code, imageBase64);
  }

  public boolean verify(String id, String code) {
    CaptchaValue value = store.remove(id);
    if (value == null) {
      return false;
    }
    if (Instant.now().isAfter(value.expireAt())) {
      return false;
    }
    return value.code().equalsIgnoreCase(code);
  }

  private String randomCode() {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      int index = random.nextInt(chars.length());
      builder.append(chars.charAt(index));
    }
    return builder.toString();
  }

  private String generateImage(String code) {
    int width = 120;
    int height = 40;
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();

    g.setColor(new Color(240, 240, 240));
    g.fillRect(0, 0, width, height);

    g.setColor(Color.LIGHT_GRAY);
    for (int i = 0; i < 10; i++) {
      int x1 = random.nextInt(width);
      int y1 = random.nextInt(height);
      int x2 = random.nextInt(width);
      int y2 = random.nextInt(height);
      g.drawLine(x1, y1, x2, y2);
    }

    for (int i = 0; i < 20; i++) {
      int x = random.nextInt(width);
      int y = random.nextInt(height);
      g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
      g.drawOval(x, y, 1, 1);
    }

    Font[] fonts = {
      new Font("Arial", Font.BOLD, 28),
      new Font("Georgia", Font.BOLD, 28),
      new Font("Times New Roman", Font.BOLD, 28),
      new Font("Verdana", Font.BOLD, 28)
    };

    char[] chars = code.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      g.setFont(fonts[random.nextInt(fonts.length)]);
      int r = random.nextInt(80) + 80;
      int gVal = random.nextInt(80) + 80;
      int b = random.nextInt(80) + 80;
      g.setColor(new Color(r, gVal, b));
      
      double angle = (random.nextDouble() - 0.5) * 0.4;
      g.rotate(angle, 20 + i * 25, height / 2);
      g.drawString(String.valueOf(chars[i]), 20 + i * 25, height / 2 + 10);
      g.rotate(-angle, 20 + i * 25, height / 2);
    }

    g.dispose();

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, "PNG", baos);
      return Base64.getEncoder().encodeToString(baos.toByteArray());
    } catch (IOException e) {
      return "";
    }
  }

  public record CaptchaResult(String id, String code, String imageBase64) {}
}
