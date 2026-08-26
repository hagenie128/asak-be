package com.asak.user.dto.menu;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 메뉴 옵션 아이템

// {
//   "optionItemId": 269,
//   "ingredientId": 105,
//   "name": "크리미칠리",
//   "extraPrice": 0,
//   "originalPrice": null,
//   "servingAmount": 50,
//   "servingUnit": "g",
//   "isRecommended": true,
//   "isDefault": true,
//   "isSoldOut": false
//    private BigDecimal kcal -> 옵션 칼로리 추가시
//    private BigDecimal protein; -> 단백질 추가
// }

@Getter
@Setter
@Alias("optionItem")
@NoArgsConstructor
public class OptionItemResponse {

  private Long optionItemId;
  private Long ingredientId;
  private String name;
  private Integer extraPrice;
  private Integer originalPrice;
  private BigDecimal servingAmount;
  private String servingUnit;
  private BigDecimal kcal;
  private BigDecimal protein; //단백질
  private Boolean isRecommended;
  private Boolean isDefault;
  private Boolean isSoldOut;
}
