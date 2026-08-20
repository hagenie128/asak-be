package com.asak.admin.dto.response.sales.daily;

import com.asak.admin.dto.response.sales.SalesShareResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailySalesBreakdownResponse {

  private List<SalesShareResponse> paymentShare;

  private List<SalesShareResponse> orderShare;
}
