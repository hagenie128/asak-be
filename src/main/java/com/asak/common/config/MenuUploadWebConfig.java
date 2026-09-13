package com.asak.common.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MenuUploadWebConfig implements WebMvcConfigurer {

  @Value("${app.file.menu-upload-dir}")
  private String menuUploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = Path.of(menuUploadDir).toAbsolutePath().normalize().toUri().toString();
    if (!location.endsWith("/")) {
      location = location + "/";
    }
    registry.addResourceHandler("/uploads/menu/**").addResourceLocations(location);
  }
}
