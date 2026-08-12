package com.asak.user.dto.payment.query;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//결제 수단 조회 DTO
@Getter
@Setter
@NoArgsConstructor
@Alias("PaymentMethodContext")
public class PaymentMethodContext {

    private Long methodId; //결제수단 종류
    private boolean enable; //활성화 유무

}
