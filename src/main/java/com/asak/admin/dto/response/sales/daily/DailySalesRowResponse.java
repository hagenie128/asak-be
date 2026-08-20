package com.asak.admin.dto.response.sales.daily;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesRowResponse {

  private LocalDate date;

  private long orderCount;
  private long canceledOrderCount;

  private long grossSalesAmount;
  private long canceledAmount;
  private long totalAmount;
  private long avgAmount;

  private double cancelRate;
}
