package com.asak.admin.service;

import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.asak.common.util.FileUtil;

@Service
public class AdminMenuService {

  @Value("${app.file.menu-upload-dir}")
  private String menuUploadDir;

  public void createMenu(MultipartFile imageFile) throws IOException {
    String imageUrl = FileUtil.saveMenuImage(
        imageFile,
        Paths.get(menuUploadDir)
    );

    // imageUrl 값을 menu.image_url 컬럼에 저장
    // 예: /uploads/menu/UUID.png
  }
}