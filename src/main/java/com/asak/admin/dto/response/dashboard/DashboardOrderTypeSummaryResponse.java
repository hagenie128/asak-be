package com.asak.admin.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOrderTypeSummaryResponse {

  private int eatIn;
  private int takeOut;
}
