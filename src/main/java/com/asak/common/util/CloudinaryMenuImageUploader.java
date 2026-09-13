package com.asak.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CloudinaryMenuImageUploader {

  private static final String FOLDER = "asak/menu";

  private final String cloudName;
  private final String apiKey;
  private final String apiSecret;
  private final RestClient restClient = RestClient.create();

  public CloudinaryMenuImageUploader(
      @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
      @Value("${CLOUDINARY_API_KEY:}") String apiKey,
      @Value("${CLOUDINARY_API_SECRET:}") String apiSecret) {
    this.cloudName = cloudName == null ? "" : cloudName.trim();
    this.apiKey = apiKey == null ? "" : apiKey.trim();
    this.apiSecret = apiSecret == null ? "" : apiSecret.trim();
  }

  public boolean isConfigured() {
    return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
  }

  public UploadedImage upload(MultipartFile file) throws IOException {
    long timestamp = Instant.now().getEpochSecond();
    String signature = sign("folder=" + FOLDER + "&timestamp=" + timestamp + apiSecret);

    ByteArrayResource filePart =
        new ByteArrayResource(file.getBytes()) {
          @Override
          public String getFilename() {
            String name = file.getOriginalFilename();
            return name == null || name.isBlank() ? "menu-image" : name;
          }
        };

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", filePart);
    body.add("api_key", apiKey);
    body.add("timestamp", String.valueOf(timestamp));
    body.add("folder", FOLDER);
    body.add("signature", signature);

    JsonNode json =
        restClient
            .post()
            .uri("https://api.cloudinary.com/v1_1/{cloud}/image/upload", cloudName)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .retrieve()
            .body(JsonNode.class);

    if (json == null || json.path("secure_url").asText("").isBlank()) {
      throw new IOException("Cloudinary 업로드 응답이 비어 있습니다.");
    }

    return new UploadedImage(
        json.path("public_id").asText(),
        json.path("secure_url").asText(),
        json.path("format").asText(null),
        json.path("width").isInt() ? json.path("width").asInt() : null,
        json.path("height").isInt() ? json.path("height").asInt() : null,
        json.path("bytes").isInt() ? json.path("bytes").asInt() : (int) file.getSize(),
        FOLDER);
  }

  private static String sign(String payload) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hashed.length * 2);
      for (byte value : hashed) {
        hex.append(String.format("%02x", value));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-1 알고리즘을 사용할 수 없습니다.", e);
    }
  }

  public record UploadedImage(
      String publicId,
      String url,
      String format,
      Integer width,
      Integer height,
      Integer bytes,
      String folder) {}
}
