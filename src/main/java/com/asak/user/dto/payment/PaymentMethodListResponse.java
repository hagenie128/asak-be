package com.asak.user.dto.payment;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//     "methods": [
    //       {
    //         "methodId": 10828,
    //         "methodCode": "CARD",
    //         "methodName": "카드·삼성페이",
    //         "isEnabled": true,
    //         "sortOrder": 1
    //       },
    //       {
    //         "methodId": 10829,
    //         "methodCode": "KAKAO_PAY",
    //         "methodName": "카카오페이",
    //         "isEnabled": false,
    //         "sortOrder": 2
    //       },
    //       {
    //         "methodId": 10830,
    //         "methodCode": "NAVER_PAY",
    //         "methodName": "네이버페이",
    //         "isEnabled": false,
    //         "sortOrder": 3
    //       }
    //     ]


// 결제수단 methods들의 list[]
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodListResponse {

    private List<PaymentMethodResponse> methods;

}
