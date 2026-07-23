// /uploads/menu/** 요청을 로컬 uploads/menu 폴더와 연결하는 설정
// 메뉴 API는 DB의 imageUrl을 그대로 반환하고,
// 브라우저가 그 URL로 요청하면 이 설정이 실제 파일을 제공한다.

package com.asak.common.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${app.file.menu-upload-dir}")
  private String menuUploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String uploadLocation = Paths.get(menuUploadDir)
        .toAbsolutePath()
        .normalize()
        .toUri()
        .toString();

    registry.addResourceHandler("/uploads/menu/**")
        .addResourceLocations(uploadLocation);
  }
}