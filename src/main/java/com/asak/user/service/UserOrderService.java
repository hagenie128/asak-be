package com.asak.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.asak.common.enums.OrderStatus;
import com.asak.common.enums.OrderType;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.user.dto.order.internal.ValidatedOptionItem;
import com.asak.user.dto.order.internal.ValidatedOrderItem;
import com.asak.user.dto.order.internal.ValidatedOrderResult;
import com.asak.user.dto.order.query.MenuQueryDto;
import com.asak.user.dto.order.query.OptionItemQueryDto;
import com.asak.user.dto.order.query.OptionPolicyQueryDto;
import com.asak.user.dto.order.request.CartValidateItemRequest;
import com.asak.user.dto.order.request.CartValidateRequest;
import com.asak.user.dto.order.request.CreateOrderRequest;
import com.asak.user.dto.order.request.OptionItemRequest;
import com.asak.user.dto.order.request.OrderItemCommand;
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

    
    //장바구니 & 주문 생성 공통 로직 통힙
    private ValidatedOrderResult validateAndPriceItems(
            List<? extends OrderItemCommand> requestedItems) {

        validateItemQuantityLimits(
                requestedItems,
                OrderItemCommand::getQuantity);

        int totalAmount = 0;
        List<ValidatedOrderItem> validatedItems = new ArrayList<>();

        for (OrderItemCommand item : requestedItems) {

            // 1. 메뉴 확인
            MenuQueryDto menu = orderMapper.selectByMenuId(item.getMenuId());

            if (menu == null) {
                throw new CustomException(
                        ErrorCode.MENU_NOT_FOUND);
            }

            // 2. 메뉴 품절 확인
            if (Boolean.TRUE.equals(menu.isSoldOut())) {
                throw new CustomException(
                        ErrorCode.MENU_SOLD_OUT);
            }

            int unitPrice = menu.getPrice();

            // null이면 빈 목록으로 정규화
            List<OptionItemRequest> requestedOptions = item.getOptionItems() == null
                    ? List.of()
                    : item.getOptionItems();

            List<ValidatedOptionItem> validatedOptions = new ArrayList<>();

            Map<Long, Integer> selectedQuantityByPolicy = new HashMap<>();

            // 3. 옵션 검증
            for (OptionItemRequest option : requestedOptions) {

                if (option == null
                        || option.getOptionItemId() == null
                        || option.getQuantity() == null
                        || option.getQuantity() <= 0) {

                    throw new CustomException(
                            ErrorCode.INVALID_OPTION_SELECTION);
                }

                OptionItemQueryDto optionInfo = orderMapper.findByOptionItem(
                        item.getMenuId(),
                        option.getOptionItemId());

                if (optionInfo == null) {
                    throw new CustomException(
                            ErrorCode.INVALID_OPTION_SELECTION);
                }

                if (Boolean.TRUE.equals(
                        optionInfo.getIsSoldOut())) {

                    throw new CustomException(
                            ErrorCode.OPTION_ITEM_SOLD_OUT);
                }

                unitPrice += optionInfo.getExtraPrice()
                        * option.getQuantity();

                selectedQuantityByPolicy.merge(
                        optionInfo.getPolicyId(),
                        option.getQuantity(),
                        Integer::sum);

                validatedOptions.add(
                        new ValidatedOptionItem(
                                option.getOptionItemId(),
                                optionInfo.getPolicyId(),
                                option.getQuantity(),
                                optionInfo.getExtraPrice()));
            }

            // 4. 옵션 정책 검증
            List<OptionPolicyQueryDto> policies = orderMapper.findOptionPoliciesByMenuId(
                    item.getMenuId());

            for (OptionPolicyQueryDto policy : policies) {

                int selectedQuantity = selectedQuantityByPolicy.getOrDefault(
                        policy.getPolicyId(),
                        0);

                int minSelect = policy.getMinSelect() == null
                        ? 0
                        : policy.getMinSelect();

                int maxSelect = policy.getMaxSelect() == null
                        ? Integer.MAX_VALUE
                        : policy.getMaxSelect();

                int requiredMinSelect = Boolean.TRUE.equals(policy.getIsRequired())
                        ? Math.max(1, minSelect)
                        : minSelect;

                if (selectedQuantity < requiredMinSelect
                        || selectedQuantity > maxSelect) {

                    throw new CustomException(
                            ErrorCode.INVALID_OPTION_SELECTION);
                }
            }

            // 5. 제외 재료 검증
            List<Long> excludedIngredientIds = item.getExcludedIngredientIds() == null
                    ? List.of()
                    : item.getExcludedIngredientIds();

            for (Long ingredientId : excludedIngredientIds) {

                Long validIngredientId = orderMapper.findRemovableIngredient(
                        item.getMenuId(),
                        ingredientId);

                if (validIngredientId == null) {
                    throw new CustomException(
                            ErrorCode.INVALID_INGREDIENT_EXCLUSION);
                }
            }

            // 6. 금액 계산
            int lineAmount = unitPrice * item.getQuantity();

            totalAmount += lineAmount;

            validatedItems.add(
                    new ValidatedOrderItem(
                            item.getMenuId(),
                            item.getQuantity(),
                            unitPrice,
                            validatedOptions,
                            excludedIngredientIds));
        }

        return new ValidatedOrderResult(
                totalAmount,
                validatedItems);
    }


    // ------------ 장바구니 api-004 ------------
    public CartValidateResponse cartValidate(CartValidateRequest request) {
        if (request == null) {
            throw new CustomException(ErrorCode.CART_EMPTY);
        }
        
        ValidatedOrderResult result = validateAndPriceItems(request.getItems());
        
        List<CartValidateItemResponse> responseItems = result.items().stream()
        .map(this::toCartValidateItemResponse)
        .toList();
        
        CartValidateResponse response = new CartValidateResponse();
        
        response.setTotalAmount(result.totalAmount());
        response.setItems(responseItems);
        
        return response;
    }
    
    // 공통 로직을 통해 -> api-004 장바구니로 변환
    private CartValidateItemResponse toCartValidateItemResponse(
            ValidatedOrderItem item) {

        CartValidateItemResponse response = new CartValidateItemResponse();

        response.setMenuId(item.menuId());
        response.setQuantity(item.quantity());
        response.setUnitPrice(item.unitPrice());
        response.setExcludedIngredientIds(
                item.excludedIngredientIds());

        List<CartValidateOptionItemResponse> options = item.optionItems().stream()
                .map(option -> {
                    CartValidateOptionItemResponse optionResponse = new CartValidateOptionItemResponse();

                    optionResponse.setOptionItemId(
                            option.optionItemId());
                    optionResponse.setQuantity(
                            option.quantity());

                    return optionResponse;
                })
                .toList();

        response.setOptionItems(options);

        return response;
    }


    /**
     * 주문/장바구니 공통 수량 제한 검증
     * 각 장바구니 아이템은 1~9개, 모든 아이템 수량의 합계는 30개까지 허용
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
     * 제거 가능한 재료인지 확인한다.
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

        // 주문 생성 api-005
        // @Transactional
        // public CreateOrderResponse createOrder(CreateOrderRequest request) {

        //     //CreateOrderRequest 자체가 없는 경우를 차단(주문 요청이 올바르지 않음 에러)
        //     if (request == null) {
        //         throw new CustomException(
        //                 ErrorCode.INVALID_ORDER_REQUEST);
        //     }

        //     //take_out || eat_in 유형 검증
        //     validateOrderType(request.getOrderType());

        //     ValidatedOrderResult result = validateAndPriceItems(request.getItems());

        //     Long orderTypeId = orderMapper.findOrderTypeId(
        //             request.getOrderType().name());

        //     Long receivedStatusId = orderMapper.findOrderStatusId("RECEIVED");

        //     String orderNo = generateOrderNo();

        //     OrderInsertDto order = new OrderInsertDto();
        //     order.setOrderNo(orderNo);
        //     order.setOrderTypeId(orderTypeId);
        //     order.setStatusId(receivedStatusId);
        //     order.setTotalAmount(result.totalAmount());

        //     orderMapper.insertOrder(order);

        //     Long orderId = order.getOrderId();

        //     for (ValidatedOrderItem item : result.items()) {

        //         OrderItemInsertDto orderItem = new OrderItemInsertDto();

        //         orderItem.setOrderId(orderId);
        //         orderItem.setMenuId(item.menuId());
        //         orderItem.setQuantity(item.quantity());
        //         orderItem.setUnitPrice(item.unitPrice());

        //         orderMapper.insertOrderItem(orderItem);

        //         Long orderItemId = orderItem.getOrderItemId();

        //         for (ValidatedOptionItem option : item.optionItems()) {

        //             orderMapper.insertOrderItemOption(
        //                     orderItemId,
        //                     option.optionItemId(),
        //                     option.quantity(),
        //                     option.extraPrice());
        //         }

        //         for (Long ingredientId : item.excludedIngredientIds()) {

        //             orderMapper.insertItemExclusion(
        //                     orderItemId,
        //                     ingredientId);
        //         }
        //     }

        //     CreateOrderResponse response = new CreateOrderResponse();

        //     response.setOrderId(orderId);
        //     response.setOrderNo(orderNo);
        //     response.setTotalAmount(result.totalAmount());
        //     response.setStatus("RECEIVED");

        //     return response;
        // }

        @Transactional
        public CreateOrderResponse createOrder(
                CreateOrderRequest request) {

            // 1. 요청 객체 확인
            if (request == null) {
                throw new CustomException(
                        ErrorCode.INVALID_ORDER_REQUEST);
            }

            // 2. 주문 유형 확인
            validateOrderType(request.getOrderType());

            // 3. 메뉴·옵션·제외 재료 검증 및 서버 가격 계산
            ValidatedOrderResult result = validateAndPriceItems(
                    request.getItems());

            // 4. 공통 코드 조회
            String orderTypeCode = request.getOrderType().name();

            String initialStatus = OrderStatus.RECEIVED.name();

            Long orderTypeId = orderMapper.findOrderTypeId(
                    orderTypeCode);

            if (orderTypeId == null) {
                throw new CustomException(
                        ErrorCode.INVALID_ORDER_TYPE);
            }

            Long statusId = orderMapper.findOrderStatusId(
                    initialStatus);

            if (statusId == null) {
                throw new CustomException(
                        ErrorCode.ORDER_STATUS_NOT_FOUND);
            }

            // 5. 주문번호 생성
            String orderNo = generateOrderNo();

            // 6. 주문 헤더 저장
            OrderInsertDto order = new OrderInsertDto();

            order.setOrderNo(orderNo);
            order.setOrderTypeId(orderTypeId);
            order.setStatusId(statusId);
            order.setTotalAmount(
                    result.totalAmount());

            int insertedOrderCount = orderMapper.insertOrder(order);

            if (insertedOrderCount != 1
                    || order.getOrderId() == null) {

                throw new CustomException(
                        ErrorCode.ORDER_CREATE_FAILED);
            }

            Long orderId = order.getOrderId();

            // 7. 주문 아이템 저장
            for (ValidatedOrderItem item : result.items()) {

                OrderItemInsertDto orderItem = new OrderItemInsertDto();

                orderItem.setOrderId(orderId);
                orderItem.setMenuId(item.menuId());
                orderItem.setQuantity(item.quantity());
                orderItem.setUnitPrice(item.unitPrice());

                int insertedItemCount = orderMapper.insertOrderItem(
                        orderItem);

                if (insertedItemCount != 1
                        || orderItem.getOrderItemId() == null) {

                    throw new CustomException(
                            ErrorCode.ORDER_ITEM_CREATE_FAILED);
                }

                Long orderItemId = orderItem.getOrderItemId();

                // 8. 선택 옵션 저장
                for (ValidatedOptionItem option : item.optionItems()) {

                    int insertedOptionCount = orderMapper
                            .insertOrderItemOption(
                                    orderItemId,
                                    option.optionItemId(),
                                    option.quantity(),
                                    option.extraPrice());

                    if (insertedOptionCount != 1) {
                        throw new CustomException(
                                ErrorCode.ORDER_OPTION_CREATE_FAILED);
                    }
                }

                // 9. 제외 재료 저장
                for (Long ingredientId : item.excludedIngredientIds()) {

                    int insertedExclusionCount = orderMapper
                            .insertItemExclusion(
                                    orderItemId,
                                    ingredientId);

                    if (insertedExclusionCount != 1) {
                        throw new CustomException(
                                ErrorCode.ORDER_EXCLUSION_CREATE_FAILED);
                    }
                }
            }

            // 10. 응답 생성
            CreateOrderResponse response = new CreateOrderResponse();

            response.setOrderId(orderId);
            response.setOrderNo(orderNo);
            response.setTotalAmount(
                    result.totalAmount());
            response.setStatus(initialStatus);

            return response;
        }

        //주문 형식 확인하는 메서드
        private void validateOrderType(OrderType orderType) {
            if (orderType == null) {
                throw new CustomException(
                        ErrorCode.INVALID_ORDER_TYPE);
            }
        }

}
