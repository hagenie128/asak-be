package com.asak.user.dto.payment;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// "data": {
//     "methods": [
//       {
//         "methodId": 10828,
//         "methodCode": "CARD",
//         "methodName": "카드 / 삼성페이 결제",
//         "imageAssetId": 352,
//         "imageUrl": "/api/assets/101",
//         "description": "신용·체크카드",
//         "active": true,
//         "sortOrder": 1
//       },
//       {
//         "methodId": 10829,
//         "methodCode": "KAKAO_PAY",
//         "methodName": "카카오페이 결제",
//         "imageAssetId": 353,
//         "imageUrl": "/api/assets/101",
//         "description": "모바일 간편결제",
//         "active": true,
//         "sortOrder": 2
//       },
//       {
//         "methodId": 10830,
//         "methodCode": "NAVER_PAY",
//         "methodName": "네이버페이 결제",
//         "imageAssetId": 354,
//         "imageUrl": "/api/assets/101",
//         "description": "모바일 간편결제",
//         "active": true,
//         "sortOrder": 3
//       },
//       {
//         "methodId": 10832,
//         "methodCode": "TOSS_PAY",
//         "methodName": "토스페이 결제",
//         "imageAssetId": 355,
//         "imageUrl": "/api/assets/101",
//         "description": "모바일 간편결제",
//         "active": true,
//         "sortOrder": 4
//       }
//     ]
//   }

// 결제수단 methods들의 list[]
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodListResponse {

  private List<PaymentMethodResponse> methods;
}
