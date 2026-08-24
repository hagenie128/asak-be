package com.asak.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** UpdatePaymentMethodRequest */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentMethodRequest {

  private Boolean active;
  private Integer sortNo;
}
