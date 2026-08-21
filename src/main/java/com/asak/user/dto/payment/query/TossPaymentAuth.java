package com.asak.user.dto.payment.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentAuth {

  @NotBlank
  @Size(max = 200)
  private String paymentKey;

  @NotBlank
  @Size(min = 6, max = 64)
  private String orderId;

  @NotNull @Positive private Long amount;
}
