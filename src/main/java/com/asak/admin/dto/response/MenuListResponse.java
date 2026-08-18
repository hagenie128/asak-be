package com.asak.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Alias("AdminMenuListResponse")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 상단 카드 목록용: 재료는 넣지 않음
public class MenuListResponse {
  private Long menuId;
  private Long categoryId;
  private String name;
  private int price;
  private String imageUrl;
  private boolean isSoldOut;
  private boolean hasSoldOutIngredient;
  private boolean isOrderable;
}
