package com.asak.admin.dto.response.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesKpiResponse {
  private String label;
  private long value;
  private String display;

  // Summary에서만 사용
  private Double delta;
  private String deltaLabel;
}
