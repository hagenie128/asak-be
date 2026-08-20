package com.asak.admin.mapper;

import com.asak.admin.dto.response.sales.DailySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.DailyTopMenuResponse;
import com.asak.admin.dto.response.sales.HourlySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.HourlyTopMenuResponse;
import com.asak.admin.dto.response.sales.MonthlySalesSummaryItemResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.asak.admin.dto.response.dashboard.AdminDashboardResponse;

public interface AdminSalesMapper {

  List<DailySalesSummaryItemResponse> getSalesSummary(Map<String, Object> dateRange);

  List<HourlySalesSummaryItemResponse> getSalesHourly(LocalDate date);

  List<HourlySalesSummaryItemResponse> getDailySalesTimeSlots(Map<String, Object> params);

  List<DailyTopMenuResponse> getDailyTopMenu(LocalDate date);

  List<HourlyTopMenuResponse> getHourlyTopMenu(LocalDate date);

  List<DailySalesSummaryItemResponse> getMonthlyDailySales(Map<String, Object> dateRange);

  int getMinYear();

  Map<String, Object> getDashboardKpi(LocalDate date);

  List<AdminDashboardResponse.RecentOrder> getDashboardRecentOrders();

  List<AdminDashboardResponse.CountSummary> getDashboardStatusSummary(LocalDate date);

  Map<String, Object> getDashboardOrderTypeSummary(LocalDate date);

  Map<String, Object> getDashboardInventorySummary();

  List<DailySalesSummaryItemResponse> getDashboardWeeklySales(Map<String, Object> dateRange);
}
