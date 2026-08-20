package com.asak.admin.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWeeklySalesResponse {

  private String label;
  private long amount;
  private Boolean isCurrent;
}
