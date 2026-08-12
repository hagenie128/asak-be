package com.asak.user.dto.payment;


import org.apache.ibatis.type.Alias;

import com.asak.common.enums.PaymentMethod;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//       {
    //         "methodId": 10828,
    //         "methodCode": "CARD",
    //         "methodName": "카드·삼성페이",
    //         "isEnabled": true,
    //         "sortOrder": 1
    //       },

// 결제수단 1개의 종류 {}
@Getter
@Setter
@NoArgsConstructor
@Alias("PaymentMethodDTO")
public class PaymentMethodResponse {

    private Long methodId;
    private PaymentMethod methodCode;
    private String methodName;
    private boolean isEnabled;
    private int sortOrder;


}
