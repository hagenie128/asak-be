package com.asak.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
import com.asak.user.dto.query.OptionPolicyQueryDTO;
import com.asak.user.mapper.UserOrderMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserOrderService {

    private final UserOrderMapper orderMapper;


    //장바구니 검증 api-004
        public CartValidateResponse cartValidate(CartValidateRequest request) {

            if (request == null) {
                throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
            }
            validateItemQuantityLimits(request.getItems(), CartValidateItemRequest::getQuantity);

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

                int unitPrice = menu.getPrice();
                List<CartValidateOptionItemResponse> validatedOptions = new ArrayList<>();
                List<OptionItemRequest> requestedOptions = item.getOptionItems() == null
                        ? List.of()
                        : item.getOptionItems();
                Map<Long, Integer> selectedQuantityByPolicy = new HashMap<>();

                // 3단계. 옵션 검증
                for (OptionItemRequest option : requestedOptions) {
                    if (option == null
                            || option.getOptionItemId() == null
                            || option.getQuantity() == null
                            || option.getQuantity() <= 0) {
                        throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                    }

                    OptionItemQueryDTO optionItem =
                            orderMapper.findByOptionItem(item.getMenuId(), option.getOptionItemId());

                    if (optionItem == null) {
                        throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                    }

                    // 옵션 선택 아이템이 품절인 경우
                    if (Boolean.TRUE.equals(optionItem.getIsSoldOut())) {
                        throw new CustomException(ErrorCode.OPTION_ITEM_SOLD_OUT);
                    }

                    unitPrice += optionItem.getExtraPrice() * option.getQuantity();
                    selectedQuantityByPolicy.merge(
                            optionItem.getPolicyId(), option.getQuantity(), Integer::sum);

                    CartValidateOptionItemResponse optionResponse = new CartValidateOptionItemResponse();
                    optionResponse.setOptionItemId(option.getOptionItemId());
                    optionResponse.setQuantity(option.getQuantity());
                    validatedOptions.add(optionResponse);
                }

                // 3-1단계. 메뉴별 옵션 정책의 필수/최소/최대 선택 수 검증
                for (OptionPolicyQueryDTO policy : orderMapper.findOptionPoliciesByMenuId(item.getMenuId())) {
                    int selectedQuantity = selectedQuantityByPolicy.getOrDefault(policy.getPolicyId(), 0);
                    int minSelect = policy.getMinSelect() == null ? 0 : policy.getMinSelect();
                    int maxSelect = policy.getMaxSelect() == null ? Integer.MAX_VALUE : policy.getMaxSelect();
                    int requiredMinSelect = Boolean.TRUE.equals(policy.getIsRequired())
                            ? Math.max(1, minSelect)
                            : minSelect;

                    if (selectedQuantity < requiredMinSelect || selectedQuantity > maxSelect) {
                        throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                    }
                }

                // 3-2단계. 제외 재료 검증
                List<Long> excludedIds = item.getExcludedIngredientIds() == null
                        ? List.of()
                        : item.getExcludedIngredientIds();
                for (Long ingredientId : excludedIds) {
                    Long validIngId = orderMapper.findRemovableIngredient(item.getMenuId(), ingredientId);
                    if (validIngId == null) {
                        throw new CustomException(ErrorCode.INVALID_INGREDIENT_EXCLUSION);
                    }
                }

                // 4단계. 수량 적용
                int lineAmount = unitPrice * item.getQuantity();
                totalAmount += lineAmount;

                // 5단계. 응답 객체 생성
                CartValidateItemResponse itemResponse = new CartValidateItemResponse();
                itemResponse.setMenuId(item.getMenuId());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setUnitPrice(unitPrice);
                itemResponse.setOptionItems(validatedOptions);
                itemResponse.setExcludedIngredientIds(excludedIds);

                items.add(itemResponse);
            }

            CartValidateResponse response = new CartValidateResponse();
            response.setTotalAmount(totalAmount);
            response.setItems(items);

            return response;
        }

    /**
     * 주문/장바구니 공통 수량 제한을 검증한다.
     * 각 장바구니 아이템은 1~9개, 모든 아이템 수량의 합계는 30개까지 허용한다.
     */
    private <T> void validateItemQuantityLimits(
            List<T> items,
            Function<T, Integer> quantityExtractor) {

        if (items == null) {
            throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
        }

        int totalQuantity = 0;
        for (T item : items) {
            Integer quantity = item == null ? null : quantityExtractor.apply(item);
            if (quantity == null || quantity <= 0) {
                throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
            }
            if (quantity > 9) {
                throw new CustomException(ErrorCode.ITEM_QUANTITY_LIMIT_EXCEEDED);
            }

            totalQuantity += quantity;
            if (totalQuantity > 30) {
                throw new CustomException(ErrorCode.CART_QUANTITY_LIMIT_EXCEEDED);
            }
        }
    }

        

    // ------ 주문 생성 API 플로우 ----
    // ① common_code에서 orderTypeId 조회
    // ② common_code에서 RECEIVED 상태 조회
    // ③ 메뉴 가격 조회
    // ④ 옵션 가격 조회
    // ⑤ totalAmount 계산
    // ⑥ orders INSERT
    // ⑦ 생성된 orderId 반환

    // {"orderType": "TAKE_OUT", 
    // "items": [
    //       {"menuId": 364, 
    //       "quantity": 1, 
    //       "optionItems": [
    //        {"optionItemId": 101,
    //         "quantity": 1}
    //    ],
    //   "excludedIngredientIds": []}
    //     ]}
    
    // 주문 생성  api-005
    @Transactional(readOnly = false)
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
        }
        validateItemQuantityLimits(request.getItems(), OrderItemRequest::getQuantity);

        //주문 총 가격
        int totalAmount = 0;

        //1. 메뉴의 품절 유무
        for(OrderItemRequest item : request.getItems() ){

            MenuQueryDTO menu = orderMapper.selectByMenuId(item.getMenuId());

            if(menu == null){
                throw new CustomException(ErrorCode.MENU_NOT_FOUND);
            }
            
            if(menu.isSoldOut()){
                throw new CustomException(ErrorCode.MENU_SOLD_OUT);
            }

            for(OptionItemRequest opi : item.getOptionItems()){
                OptionItemQueryDTO opInfo =  orderMapper.findByOptionItem(item.getMenuId(),opi.getOptionItemId());
                
                
                if(opInfo == null){
                    throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                }

                if(Boolean.TRUE.equals(opInfo.getIsSoldOut())){
                    throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                }
    
            }
        }




        OrderType orderType = request.getOrderType();
        List<OrderItemRequest> items = request.getItems();

        return null;
    }

}


