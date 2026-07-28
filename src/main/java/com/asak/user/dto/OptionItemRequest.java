package com.asak.user.dto;

import org.apache.ibatis.type.Alias;

// 장바구니 주문1건 아이템1건에 대한 옵션값들 요청하는그릇

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Alias("cartOptionItem")
public class OptionItemRequest {

  private Long optionItemId;
  private Integer quantity;
}
