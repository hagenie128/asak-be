package com.asak.admin.mapper;

import com.asak.admin.dto.response.sales.DailySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.DailyTopMenuResponse;
import com.asak.admin.dto.response.sales.HourlySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.HourlyTopMenuResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdminSalesMapper {

  List<DailySalesSummaryItemResponse> getSalesSummary(Map<String, Object> dateRange);

  List<HourlySalesSummaryItemResponse> getSalesHourly(LocalDate date);

  List<DailyTopMenuResponse> getDailyTopMenu(LocalDate date);

  List<HourlyTopMenuResponse> getHourlyTopMenu(LocalDate date);
}
