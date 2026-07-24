package com.asak.admin.dto.request;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

// API-007 GET /api/admin/orders 조회 조건. status/orderType는 code 문자열 그대로 받는다.
@Getter
@Builder
public class OrderListFilter {
  private String status;
  private String orderType;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private String keyword;
  private int offset;
  private int limit;
}
