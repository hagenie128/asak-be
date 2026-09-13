// AdminMenuService: 메뉴 이미지 업로드·삭제에 사용
// UserMenuService: DB의 imageUrl 값을 그대로 응답에 반환

package com.asak.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.web.multipart.MultipartFile;

public final class FileUtil {

  private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

  private static final String MENU_UPLOAD_URL_PREFIX = "/uploads/menu/";

  private static final Pattern STORED_FILE_NAME_PATTERN =
      Pattern.compile("^[0-9a-f-]{36}\\.(jpg|png|webp)$", Pattern.CASE_INSENSITIVE);

  private static final Map<String, String> IMAGE_EXTENSIONS =
      Map.of(
          "image/jpeg", ".jpg",
          "image/png", ".png",
          "image/webp", ".webp");

  private FileUtil() {}

  public static void validateMenuImage(MultipartFile file) {
    validateImage(file);
  }

  public static String saveMenuImage(MultipartFile file, Path menuUploadDirectory)
      throws IOException {
    validateImage(file);

    Files.createDirectories(menuUploadDirectory);

    String contentType = file.getContentType().toLowerCase(Locale.ROOT);
    String storedFileName = UUID.randomUUID() + IMAGE_EXTENSIONS.get(contentType);

    Path rootPath = menuUploadDirectory.toAbsolutePath().normalize();
    Path targetPath = rootPath.resolve(storedFileName).normalize();

    if (!targetPath.startsWith(rootPath)) {
      throw new IllegalArgumentException("잘못된 파일 저장 경로입니다.");
    }

    file.transferTo(targetPath);

    return MENU_UPLOAD_URL_PREFIX + storedFileName;
  }

  public static void deleteMenuImage(String imageUrl, Path menuUploadDirectory) throws IOException {
    if (imageUrl == null || !imageUrl.startsWith(MENU_UPLOAD_URL_PREFIX)) {
      return;
    }

    String storedFileName = imageUrl.substring(MENU_UPLOAD_URL_PREFIX.length());

    if (!STORED_FILE_NAME_PATTERN.matcher(storedFileName).matches()) {
      throw new IllegalArgumentException("잘못된 업로드 파일명입니다.");
    }

    Path rootPath = menuUploadDirectory.toAbsolutePath().normalize();
    Path targetPath = rootPath.resolve(storedFileName).normalize();

    if (!targetPath.startsWith(rootPath)) {
      throw new IllegalArgumentException("잘못된 파일 삭제 경로입니다.");
    }

    Files.deleteIfExists(targetPath);
  }

  private static void validateImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("이미지 파일이 필요합니다.");
    }

    if (file.getSize() > MAX_IMAGE_SIZE) {
      throw new IllegalArgumentException("이미지는 5MB 이하만 가능합니다.");
    }

    String contentType = file.getContentType();

    if (contentType == null
        || !IMAGE_EXTENSIONS.containsKey(contentType.toLowerCase(Locale.ROOT))) {
      throw new IllegalArgumentException("JPG, PNG, WEBP 이미지만 업로드할 수 있습니다.");
    }
  }
}
