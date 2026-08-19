package com.asak.admin.dto.response.sales;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailySalesSummaryItemResponse {
  private Date salesDate;
  private BigInteger orderCount;
  private BigInteger canceledOrderCount;
  private BigDecimal grossSalesAmount;
  private BigDecimal canceledAmount;
  private BigDecimal netSalesAmount;
}
