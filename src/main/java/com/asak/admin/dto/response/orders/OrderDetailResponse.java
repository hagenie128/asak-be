package com.asak.admin.dto.response.orders;

import java.time.LocalDateTime;
import java.util.List;

import com.asak.admin.dto.response.AdminPaymentMethodResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
  private Long orderId;
  private String orderNo;
  private String orderType;
  private String orderStatus;
  private String paymentStatus;
  private AdminPaymentMethodResponse paymentMethod;
  private int totalAmount;
  private LocalDateTime createdAt;
  private List<OrderItemResponse> items;
}
