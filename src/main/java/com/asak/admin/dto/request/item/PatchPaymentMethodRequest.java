package com.asak.admin.dto.request.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제수단 변경 Request — PATCH /api/admin/paymentMethods/{id} 키오스크 API-014 와 동일하게 active 사용. sortOrder ·
 * receiptMessage.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchPaymentMethodRequest {

  private Boolean active;
  private Integer sortOrder;
  private String receiptMessage;
}
