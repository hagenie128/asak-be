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
public class DailyTopMenuResponse {
  private Date salesDate;
  private BigInteger menuId;
  private String menuName;
  private BigDecimal quantity;
  private BigInteger orderCount;
  private BigDecimal salesAmount;
}