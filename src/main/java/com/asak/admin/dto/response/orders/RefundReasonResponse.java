package com.asak.admin.dto.response.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundReasonResponse {
  private String code;
  private String name;
  private Integer sortNo;
  private Boolean requiresDetail;
}
