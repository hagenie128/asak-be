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
import com.asak.user.dto.order.query.MenuQueryDto;
import com.asak.user.dto.order.query.OptionItemQueryDto;
import com.asak.user.dto.order.query.OptionPolicyQueryDto;
import com.asak.user.dto.order.request.CartValidateItemRequest;
import com.asak.user.dto.order.request.CartValidateRequest;
import com.asak.user.dto.order.request.CreateOrderRequest;
import com.asak.user.dto.order.request.OptionItemRequest;
import com.asak.user.dto.order.request.OrderItemRequest;
import com.asak.user.dto.order.response.CartValidateItemResponse;
import com.asak.user.dto.order.response.CartValidateOptionItemResponse;
import com.asak.user.dto.order.response.CartValidateResponse;
import com.asak.user.dto.order.response.CreateOrderResponse;
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
                throw new CustomException(ErrorCode.CART_EMPTY);
            }
            validateItemQuantityLimits(request.getItems(), CartValidateItemRequest::getQuantity);

            int totalAmount = 0;
            List<CartValidateItemResponse> items = new ArrayList<>();

            for (CartValidateItemRequest item : request.getItems()) {

                // 1단계. 메뉴 존재 여부 확인
                MenuQueryDto menu = orderMapper.selectByMenuId(item.getMenuId());
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

                    OptionItemQueryDto optionItem =
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
                for (OptionPolicyQueryDto policy : orderMapper.findOptionPoliciesByMenuId(item.getMenuId())) {
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

        if (items == null || items.isEmpty()) {
            throw new CustomException(ErrorCode.CART_EMPTY);
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

        

    /*
     * API-004(장바구니 검증)와 API-005(주문 생성)의 공통 처리
     *
     * 두 API 모두 클라이언트가 선택한 동일한 메뉴/옵션/제외재료 구조를 서버에서 다시 검증한다.
     * 즉, "옵션을 선택하는 UI 동작"은 클라이언트의 역할이고, 두 API의 공통 역할은 전달받은 선택 결과가
     * 현재 DB 기준으로 주문 가능한지 확인하고 서버 가격을 계산하는 것이다.
     *
     * ① 요청과 items가 비어 있지 않은지 확인한다.
     * ② 아이템별 수량(1~9)과 전체 수량(최대 30)을 확인한다.
     * ③ 메뉴가 존재하고 현재 품절 상태가 아닌지 확인한다.
     * ④ optionItems가 null이면 빈 목록으로 정규화한다.
     * ⑤ 옵션 ID와 수량이 유효하고, 해당 옵션이 해당 메뉴에 연결되어 있는지 확인한다.
     * ⑥ 옵션 품절 여부를 확인한다. 품절이면 OPTION_ITEM_SOLD_OUT을 반환한다.
     * ⑦ 옵션 정책별 필수 선택, minSelect, maxSelect를 확인한다.
     * ⑧ excludedIngredientIds가 null이면 빈 목록으로 정규화하고, 각 재료가 해당 메뉴에서
     *    제거 가능한 재료인지 확인한다.
     * ⑨ 메뉴 가격과 옵션 추가 금액을 DB에서 읽어 아이템 단가와 totalAmount를 계산한다.
     *
     * 위 ①~⑨는 API별로 복사하지 말고 validateAndPriceItems 같은 private 공통 메서드로 추출하는 것이 좋다.
     * 공통 메서드는 검증된 메뉴 가격, 옵션 가격, 정규화된 제외재료, totalAmount를 담은 내부 결과 객체를
     * 반환하게 한다. API-004는 그 결과를 CartValidateResponse로 변환하고, API-005는 같은 결과를 저장한다.
     */

    /*
     * API-005 주문 생성 전용 처리 순서
     *
     * ① orderType이 EAT_IN 또는 TAKE_OUT인지 확인한다.
     * ② 위의 API-004/API-005 공통 검증과 서버 가격 계산을 수행한다.
     * ③ common_code에서 orderType에 해당하는 orderTypeId와 RECEIVED 상태의 statusId를 조회한다.
     * ④ 명세 형식(AyyyyMMddNNNN)의 중복되지 않는 orderNo를 생성한다.
     * ⑤ orders에 orderNo, orderTypeId, statusId, totalAmount를 INSERT하고 orderId를 받는다.
     * ⑥ 검증된 각 아이템을 order_item에 INSERT하고 orderItemId를 받는다.
     * ⑦ 각 아이템의 선택 옵션과 당시 옵션 가격을 order_item_option에 INSERT한다.
     * ⑧ 각 아이템의 제외재료를 item_exclusion에 INSERT한다.
     * ⑨ orderId, orderNo, totalAmount, RECEIVED만 CreateOrderResponse에 담아 반환한다.
     *
     * 주문 헤더부터 제외재료 저장까지 하나의 쓰기 트랜잭션으로 처리한다. 중간 INSERT가 하나라도 실패하면
     * 전체 주문이 롤백되어야 한다. 클라이언트 금액은 요청에 없으므로 totalAmount는 항상 서버 계산값을 사용한다.
     */
    
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

            MenuQueryDto menu = orderMapper.selectByMenuId(item.getMenuId());

            if(menu == null){
                throw new CustomException(ErrorCode.MENU_NOT_FOUND);
            }
            
            if(menu.isSoldOut()){
                throw new CustomException(ErrorCode.MENU_SOLD_OUT);
            }

            for(OptionItemRequest opi : item.getOptionItems()){
                OptionItemQueryDto opInfo =  orderMapper.findByOptionItem(item.getMenuId(),opi.getOptionItemId());
                
                
                if(opInfo == null){
                    throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
                }

                if(Boolean.TRUE.equals(opInfo.getIsSoldOut())){
                    throw new CustomException(ErrorCode.OPTION_ITEM_SOLD_OUT);
                }
    
            }
        }




        OrderType orderType = request.getOrderType();
        List<OrderItemRequest> items = request.getItems();

        return null;
    }

}


