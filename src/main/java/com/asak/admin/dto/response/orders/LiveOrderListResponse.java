package com.asak.admin.dto.response.orders;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** API-021 active live-order board envelope payload. */
@Getter
@Builder
@AllArgsConstructor
public class LiveOrderListResponse {
  private List<LiveOrderResponse> content;
  private int totalElements;
}
