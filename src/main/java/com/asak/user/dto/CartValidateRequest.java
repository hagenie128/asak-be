package com.asak.user.dto;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//주문 1건에 대한 request 장바구니 검증

@Getter
@Setter
@NoArgsConstructor
@Alias("cart")
public class CartValidateRequest {


    private List<CartValidateItemRequest> items;


}
