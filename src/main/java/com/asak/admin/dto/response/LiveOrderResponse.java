package com.asak.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** SCR-009 live order board row. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveOrderResponse {
  private Long orderId;
  private String orderNo;
  private String orderTypeLabel;
  private String orderStatus;
  private int totalAmount;
  private LocalDateTime createdAt;
  private Long elapsedSec;

  @JsonRawValue private String menus;
}
