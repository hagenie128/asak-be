package com.asak.admin.dto.response.sales.month;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesRowResponse {

  // 예: "2026-07"
  private String month;

  private long orderCount;
  private long totalAmount;
  private long avgAmount;

  private long canceledOrders;
  private long grossSalesAmount;
  private long canceledAmount;

  private double cancelRate;
}
