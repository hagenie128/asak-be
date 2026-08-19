package com.asak.admin.service;

import com.asak.admin.dto.response.sales.DailySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.DailyTopMenuResponse;
import com.asak.admin.dto.response.sales.HourlySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.HourlyTopMenuResponse;
import com.asak.admin.mapper.AdminSalesMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// TODO-018: summary/monthly/daily/dashboard 집계 로직을 Mapper 호출과 DTO 조립으로 구현한다.
// Controller의 날짜 query를 검증된 값으로 받고, 집계 기준(주문 상태·시간대·0값)을 네 endpoint에서 일관되게 적용한다.
// view를 사용하면 실제 DB 정의와 성능을 확인하고, frontend TODO-022/025 연결 전 빈 기간 응답을 API로 검증한다.
@Service
public class AdminSalesService {

  private final AdminSalesMapper adminSalesMapper;

  public AdminSalesService(AdminSalesMapper adminSalesMapper) {
    this.adminSalesMapper = adminSalesMapper;
  }

  public List<DailySalesSummaryItemResponse> getSalesSummary(
      LocalDate startDate, LocalDate endDate) {

    Map<String, Object> dateRange = new HashMap<>();
    dateRange.put("startDate", startDate);
    dateRange.put("endDate", endDate);
    List<DailySalesSummaryItemResponse> response = adminSalesMapper.getSalesSummary(dateRange);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }

  public List<HourlySalesSummaryItemResponse> getSalesHourly(LocalDate date) {
    List<HourlySalesSummaryItemResponse> response = adminSalesMapper.getSalesHourly(date);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }

  public List<DailyTopMenuResponse> getDailyTopMenu(LocalDate date) {
    List<DailyTopMenuResponse> response = adminSalesMapper.getDailyTopMenu(date);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }

  public List<HourlyTopMenuResponse> getHourlyTopMenu(LocalDate date) {
    List<HourlyTopMenuResponse> response = adminSalesMapper.getHourlyTopMenu(date);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }
}
