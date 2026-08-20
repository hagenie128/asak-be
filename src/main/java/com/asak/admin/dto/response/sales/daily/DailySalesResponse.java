package com.asak.admin.dto.response.sales.daily;

import com.asak.admin.dto.response.sales.MenuSalesRankingResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailySalesResponse {

  private LocalDate from;
  private LocalDate to;

  private List<DailySalesRowResponse> rows;

  private Map<String, List<MenuSalesRankingResponse>> ranking;

  private Map<String, DailySalesBreakdownResponse> breakdown;
}
