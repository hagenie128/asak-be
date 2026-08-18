package com.asak.user.dto.payment;

import com.asak.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// {
//         "methodId": 10828,
//         "methodCode": "CARD",
//         "methodName": "카드 / 삼성페이 결제",
//         "imageAssetId": 352,
//          imageUrl": "/api/assets/101",
//         "description": "신용·체크카드",
//         "active": true,
//         "sortOrder": 1
//       }

// 결제수단 1개의 종류 {}
@Getter
@Setter
@NoArgsConstructor
@Alias("PaymentMethodDTO")
public class PaymentMethodResponse {

  private Long methodId;
  private PaymentMethod methodCode;
  private String methodName;
  private Long imageAssetId;
  private String imageUrl;
  private String description;
  private boolean active;
  private int sortOrder;
}
