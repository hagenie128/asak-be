package com.asak.user.dto.payment.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//결제 수단 조회 DTO
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodContext {

    private Long methodId;
    private boolean enable;

}
