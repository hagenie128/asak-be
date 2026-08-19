package com.asak.admin.dto.response.sales;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesSummaryResponse {
  private String startDate;
  private String endDate;
  private List<DailySalesSummaryItemResponse> dailySalesSummaryItems;
  private List<HourlySalesSummaryItemResponse> hourlySalesSummaryItems;
}
