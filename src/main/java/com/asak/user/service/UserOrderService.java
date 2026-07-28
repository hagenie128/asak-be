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

            //장바구니 전체 가격
            int totalPrice = 0;

            // 장바구니 검증 후 메뉴를 담을 배열
            List<CartValidateItemResponse> items = new ArrayList<>();

            for (CartValidateItemRequest item : request.getItems()) {

                MenuQueryDTO menu = orderMapper.selectByMenuId(item.getMenuId());
                if (menu == null) {
                    throw new CustomException(ErrorCode.MENU_NOT_FOUND);
                }
                if (Boolean.TRUE.equals(menu.isSoldOut())) {
                    throw new CustomException(ErrorCode.MENU_SOLD_OUT);
                }

                // 메뉴 기본 가격
                int unitPrice = menu.getPrice();

                // 메뉴의 옵션 아이템을 담을 배열
                List<CartValidateOptionItemResponse> menuItemOptions = new ArrayList<>();

                // 메뉴의 옵션 아이템을 반복
                for (OptionItemRequest option : item.getOptionItems()) {
                    OptionItemQueryDTO optionItem =
                            orderMapper.findByOptionItem(item.getMenuId(), option.getOptionItemId());

                    if (optionItem == null) {
                        throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                    }

                    unitPrice += optionItem.getExtraPrice() * option.getQuantity();

                    CartValidateOptionItemResponse optionResponse = new CartValidateOptionItemResponse();
                    optionResponse.setOptionItemId(option.getOptionItemId());
                    optionResponse.setQuantity(option.getQuantity());
                    menuItemOptions.add(optionResponse);
                }

                int itemTotalPrice = unitPrice * item.getQuantity();
                totalPrice += itemTotalPrice;

                CartValidateItemResponse itemResponse = new CartValidateItemResponse();
                itemResponse.setMenuId(item.getMenuId());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setUnitPrice(unitPrice);
                itemResponse.setOptionItems(menuItemOptions);
                itemResponse.setExcludedIngredientIds(item.getExcludedIngredientIds()); // TODO: 검증 필요

                items.add(itemResponse);
            }

            CartValidateResponse response = new CartValidateResponse();
            response.setTotalAmount(totalPrice);
            response.setItems(items);

            return response;
        }

        

    // ------ 주문 생성 API 플로우 ----
    // ① common_code에서 orderTypeId 조회
    // ② common_code에서 RECEIVED 상태 조회
    // ③ 메뉴 가격 조회
    // ④ 옵션 가격 조회
    // ⑤ totalPrice 계산
    // ⑥ orders INSERT
    // ⑦ 생성된 orderId 반환
    
    // 주문 생성  api-005
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        
        OrderType orderType = request.getOrderType();
        List<OrderItemRequest> items = request.getItems();

        return null;
    }

}


