package com.asak.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.asak.common.enums.OrderType;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import com.asak.user.dto.CartValidateItemRequest;
import com.asak.user.dto.CartValidateItemResponse;
import com.asak.user.dto.CartValidateOptionItemResponse;
import com.asak.user.dto.CartValidateRequest;
import com.asak.user.dto.CartValidateResponse;
import com.asak.user.dto.CreateOrderRequest;
import com.asak.user.dto.CreateOrderResponse;
import com.asak.user.dto.OptionItemRequest;
import com.asak.user.dto.OrderItemRequest;
import com.asak.user.dto.query.MenuQueryDTO;
import com.asak.user.dto.query.OptionItemQueryDTO;
import com.asak.user.mapper.UserOrderMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final UserOrderMapper orderMapper;


    //장바구니 검증 api-004
        public CartValidateResponse cartValidate(CartValidateRequest request) {

            int totalAmount = 0;
            List<CartValidateItemResponse> items = new ArrayList<>();

            for (CartValidateItemRequest item : request.getItems()) {

                // 1단계. 메뉴 존재 여부 확인
                MenuQueryDTO menu = orderMapper.selectByMenuId(item.getMenuId());
                if (menu == null) {
                    throw new CustomException(ErrorCode.MENU_NOT_FOUND);
                }

                // 2단계. 품절 확인
                if (Boolean.TRUE.equals(menu.isSoldOut())) {
                    throw new CustomException(ErrorCode.MENU_SOLD_OUT);
                }

                int unitAmount = menu.getPrice();
                List<CartValidateOptionItemResponse> validatedOptions = new ArrayList<>();

                // 3단계. 옵션 검증
                for (OptionItemRequest option : item.getOptionItems()) {
                    OptionItemQueryDTO optionItem =
                            orderMapper.findByOptionItem(item.getMenuId(), option.getOptionItemId());

                    if (optionItem == null) {
                        throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                    }

                    unitAmount += optionItem.getExtraPrice() * option.getQuantity();

                    CartValidateOptionItemResponse optionResponse = new CartValidateOptionItemResponse();
                    optionResponse.setOptionItemId(option.getOptionItemId());
                    optionResponse.setQuantity(option.getQuantity());
                    validatedOptions.add(optionResponse);
                }

                // 3-2단계. 제외 재료 검증
                List<Long> excludedIds = item.getExcludedIngredientIds();
                if (excludedIds != null) {
                    for (Long ingredientId : excludedIds) {
                        Long validIngId = orderMapper.findRemovableIngredient(item.getMenuId(), ingredientId);
                        if (validIngId == null) {
                            throw new CustomException(ErrorCode.INVALID_INGREDIENT_EXCLUSION);
                        }
                    }
                }

                // 4단계. 수량 적용
                int lineAmount = unitAmount * item.getQuantity();
                totalAmount += lineAmount;

                // 5단계. 응답 객체 생성
                CartValidateItemResponse itemResponse = new CartValidateItemResponse();
                itemResponse.setMenuId(item.getMenuId());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setUnitAmount(unitAmount);
                itemResponse.setOptionItems(validatedOptions);
                itemResponse.setExcludedIngredientIds(excludedIds);

                items.add(itemResponse);
            }

            CartValidateResponse response = new CartValidateResponse();
            response.setTotalAmount(totalAmount);
            response.setItems(items);

            return response;
        }

        

    // ------ 주문 생성 API 플로우 ----
    // ① common_code에서 orderTypeId 조회
    // ② common_code에서 RECEIVED 상태 조회
    // ③ 메뉴 가격 조회
    // ④ 옵션 가격 조회
    // ⑤ totalAmount 계산
    // ⑥ orders INSERT
    // ⑦ 생성된 orderId 반환
    
    // 주문 생성  api-005
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        
        OrderType orderType = request.getOrderType();
        List<OrderItemRequest> items = request.getItems();

        return null;
    }

}


