package com.asak.admin.dto.response.sales.daily;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailySalesTimeSlotResponse {

  private int salesHour;

  private int salesMinute;

  private int orderCount;

  private long netSalesAmount;

  private long averageOrderAmount;
}
