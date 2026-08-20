package com.asak.admin.dto.response.sales;

import com.asak.admin.dto.response.sales.daily.DailySalesRowResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryResponse {

  private String label;
  private String dateRange;

  private List<String> availablePeriods;

  private List<SalesKpiResponse> kpis;

  private List<DailySalesRowResponse> dailySales;

  private List<HourlySalesResponse> hourlySales;

  private List<SalesShareResponse> paymentShare;

  private List<SalesShareResponse> orderShare;

  private List<MenuSalesRankingResponse> ranking;
}
