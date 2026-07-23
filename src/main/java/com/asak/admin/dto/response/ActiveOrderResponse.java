package com.asak.admin.dto.response;

import java.time.LocalDateTime;

public class ActiveOrderResponse {
  private Long id;
  private String orderNo;
  private Long orderTypeId;
  private Long statusId;
  private Integer totalPrice;
  private LocalDateTime createdAt;
  private LocalDateTime canceledAt;
}
