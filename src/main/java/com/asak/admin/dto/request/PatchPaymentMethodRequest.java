package com.asak.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 결제수단 변경 Request — PATCH /api/admin/paymentMethods/{id} (TODO-040) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchPaymentMethodRequest {

  private String status;
  private Integer sortOrder;
  private String receiptMessage;
}
