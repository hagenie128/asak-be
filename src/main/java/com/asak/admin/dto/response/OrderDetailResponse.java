package com.asak.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
  private Long orderId;
  private String orderNo;
  private String orderType;
  private String orderStatus;
  private int totalAmount;
  private LocalDateTime createdAt;
  private List<OrderItemResponse> items;
}