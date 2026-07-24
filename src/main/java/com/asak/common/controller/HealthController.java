package com.asak.common.controller;

import java.util.Map;

import com.asak.common.response.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/health")
  public ApiResponse<Map<String, String>> health() {
    return ApiResponse.success(
        "HEALTH_OK",
        "서버가 정상입니다.",
        Map.of("service", "ASAK-backend"));
  }
}