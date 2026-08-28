package com.asak.admin.dto.request.orders;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class OrderRefundRequest {
  @NotBlank(message = "환불 사유 코드를 선택해주세요.")
  @Size(max = 50, message = "환불 사유 코드는 50자 이하여야 합니다.")
  private String refundReasonCode;

  @Size(max = 200, message = "환불 사유 상세는 200자 이하여야 합니다.")
  private String refundReasonDetail;
}
