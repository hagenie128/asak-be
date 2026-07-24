package com.asak.admin.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
  private Long itemId;
  private Long menuId;
  private String menuName;
  private int quantity;

  @JsonRawValue
  private String options; // vw_order_item_full.options 그대로 (JSON 배열 문자열)
  @JsonRawValue
  private String exclusions; // vw_order_item_full.exclusions 그대로
}