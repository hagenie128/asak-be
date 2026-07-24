package com.asak.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// API-007 GET /api/admin/orders 목록 행. DB view: vw_order_list_summary.
// 금액 필드명은 api/admin/02-order-list.bru 계약대로 totalAmount.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {
  private Long orderId;
  private String orderNo;
  private String orderType;
  private String orderStatus;
  private String paymentStatus;
  private int totalAmount;
  private LocalDateTime createdAt;
  private int itemCount;
  private String menuSummary;
}
