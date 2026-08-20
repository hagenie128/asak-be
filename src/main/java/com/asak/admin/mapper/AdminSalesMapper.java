package com.asak.admin.mapper;

import com.asak.admin.dto.response.dashboard.DashboardOrderStatusResponse;
import com.asak.admin.dto.response.dashboard.DashboardRecentOrderResponse;
import com.asak.admin.dto.response.dashboard.DashboardWeeklySalesResponse;
import com.asak.admin.dto.response.sales.HourlySalesResponse;
import com.asak.admin.dto.response.sales.MenuSalesRankingResponse;
import com.asak.admin.dto.response.sales.SalesShareResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesRowResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesTimeSlotResponse;
import com.asak.admin.dto.response.sales.month.MonthlySalesRowResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** TODO-018: SQL 결과는 새 화면 DTO의 행 단위 타입으로만 매핑한다. */
public interface AdminSalesMapper {

  int getMinYear();

  List<DailySalesRowResponse> getDailySalesRows(Map<String, Object> dateRange);

  List<HourlySalesResponse> getHourlySalesByRange(Map<String, Object> dateRange);

  List<DailySalesTimeSlotResponse> getDailySalesTimeSlots(Map<String, Object> params);

  List<MonthlySalesRowResponse> getMonthlySalesRows(int year);

  List<MenuSalesRankingResponse> getMenuRankingByRange(Map<String, Object> dateRange);

  List<SalesShareResponse> getPaymentShare(Map<String, Object> dateRange);

  List<SalesShareResponse> getOrderShare(Map<String, Object> dateRange);

  Map<String, Object> getDashboardKpi(
      @Param("today") LocalDate today, @Param("yesterday") LocalDate yesterday);

  List<DashboardRecentOrderResponse> getDashboardRecentOrders();

  List<DashboardOrderStatusResponse> getDashboardStatusSummary(LocalDate date);

  Map<String, Object> getDashboardOrderTypeSummary(LocalDate date);

  Map<String, Object> getDashboardInventorySummary();

  List<DashboardWeeklySalesResponse> getDashboardWeeklySales(Map<String, Object> dateRange);

  long getDailySales(Map<String, Object> dateRange);

  long getDailyOrderCount(Map<String, Object> dateRange);
}
