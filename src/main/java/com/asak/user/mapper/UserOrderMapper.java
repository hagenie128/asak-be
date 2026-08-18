package com.asak.user.mapper;

import com.asak.user.dto.order.internal.OrderInsertDto;
import com.asak.user.dto.order.internal.OrderItemInsertDto;
import com.asak.user.dto.order.query.MenuQueryDto;
import com.asak.user.dto.order.query.OptionItemQueryDto;
import com.asak.user.dto.order.query.OptionPolicyQueryDto;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserOrderMapper {

  // 장바구니 & 주문
  MenuQueryDto selectByMenuId(Long menuId);

  OptionItemQueryDto findByOptionItem(
      @Param("menuId") Long menuId, @Param("optionItemId") Long optionItemId);

  List<OptionPolicyQueryDto> findOptionPoliciesByMenuId(Long menuId);

  Long findRemovableIngredient(
      @Param("menuId") Long menuId, @Param("ingredientId") Long ingredientId);

  // 주문 유형 찾기
  Long findOrderTypeId(@Param("code") String code);

  // 주문 상태 찾기
  Long findOrderStatusId(@Param("code") String code);

  // 해당 날짜의 마지막 주문번호 찾기
  String findLastOrderNoByPrefix(@Param("orderNoPrefix") String orderNoPrefix);

  // 주문 헤더 저장 및 생성된 orderId 반환
  int insertOrder(OrderInsertDto order);

  // 주문 아이템 저장 및 생성된 orderItemId 반환
  int insertOrderItem(OrderItemInsertDto orderItem);

  // 주문 아이템에 따른 옵션 저장
  int insertOrderItemOption(
      @Param("orderItemId") Long orderItemId,
      @Param("optionItemId") Long optionItemId,
      @Param("quantity") int quantity,
      @Param("extraPrice") int extraPrice);

  // 주문 아이템에 제외하는 재료 저장
  int insertItemExclusion(
      @Param("orderItemId") Long orderItemId, @Param("ingredientId") Long ingredientId);
}
