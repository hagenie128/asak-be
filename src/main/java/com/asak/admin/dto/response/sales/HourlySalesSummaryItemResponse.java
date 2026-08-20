package com.asak.admin.dto.response.sales;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HourlySalesSummaryItemResponse {

  private Date salesDate;

  private Integer salesHour;

  // 0 또는 30
  private Integer salesMinute;

  private BigInteger orderCount;
  private BigInteger canceledOrderCount;

  private BigDecimal grossSalesAmount;
  private BigDecimal canceledAmount;
  private BigDecimal netSalesAmount;

  private BigDecimal averageOrderAmount;
  private BigDecimal cancelRate;
}