package com.asak.admin.dto.response.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlySalesResponse {

  private int hour;
  private long orderCount;
  private long salesAmount;
}
