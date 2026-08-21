package com.asak.admin.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRecentOrderResponse {

  private String orderNo;
  private String orderType;
  private String menuSummary;
  private long totalAmount;
  private String orderStatus;
  private String createdAtLabel;
}
