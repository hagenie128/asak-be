package com.asak.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제수단 변경 Request — PATCH /api/admin/paymentMethods/{id}
 * FE draft 필드 isActive · sortOrder · receiptMessage 와 맞춤.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchPaymentMethodRequest {

  private Boolean isActive;
  private Integer sortOrder;
  private String receiptMessage;
}
