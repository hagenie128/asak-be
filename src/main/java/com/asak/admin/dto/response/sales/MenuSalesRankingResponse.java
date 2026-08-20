package com.asak.admin.dto.response.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSalesRankingResponse {

  private int rank;
  private Long menuId;
  private String menuName;
  private int orderCount;
  private long salesAmount;
}
