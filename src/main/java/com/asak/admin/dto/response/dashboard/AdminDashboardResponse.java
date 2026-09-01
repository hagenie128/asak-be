package com.asak.admin.dto.response.dashboard;

import com.asak.admin.dto.response.sales.SalesKpiResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

  private String dateLabel;

  private List<SalesKpiResponse> kpis;

  private List<DashboardRecentOrderResponse> recentOrders;

  private List<DashboardOrderStatusResponse> statusSummary;

  private DashboardOrderTypeSummaryResponse orderTypeSummary;

  private List<DashboardInventoryAlertResponse> inventoryAlerts;

  private List<DashboardWeeklySalesResponse> weeklySales;

  /** 직전 7일 순매출. 대시보드 전주 대비 계산용. */
  private Long previousWeekNetSales;
}
