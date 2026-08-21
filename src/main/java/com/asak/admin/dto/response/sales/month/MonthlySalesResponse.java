package com.asak.admin.dto.response.sales.month;

import com.asak.admin.dto.response.sales.MenuSalesRankingResponse;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlySalesResponse {

  private int year;

  private List<MonthlySalesRowResponse> rows;

  private Map<String, List<MenuSalesRankingResponse>> ranking;
}
