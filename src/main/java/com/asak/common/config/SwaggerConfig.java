package com.asak.common.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Kiosk API")
            .description("키오스크 프로젝트 백엔드 API 문서")
            .version("v1"));
  }

  @Bean
  public GroupedOpenApi kioskApi() {
    return GroupedOpenApi.builder()
        .group("01. 키오스크")
        .pathsToMatch("/api/kiosk/**")
        .build();
  }

  @Bean
  public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("02. 관리자")
        .pathsToMatch("/api/admin/**")
        .build();
  }

  @Bean
  public GroupedOpenApi healthApi() {
    return GroupedOpenApi.builder()
        .group("00. 공통")
        .pathsToMatch("/api/health")
        .build();
  }
}


