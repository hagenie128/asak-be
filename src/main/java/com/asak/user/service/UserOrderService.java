package com.asak.user.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import com.asak.user.dto.order.internal.OrderInsertDto;
import com.asak.user.dto.order.internal.OrderItemInsertDto;
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

    //주문 생성 변수
    private static final String ORDER_NO_PREFIX = "ASAK";
    private static final ZoneId ORDER_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ORDER_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");;
    private static final int MAX_DAILY_ORDER_SEQUENCE = 999999;

    private final UserOrderMapper orderMapper;

    
    //------------ 장바구니 & 주문 생성 공통 로직 통합 ------------
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
     * ④ 명세 형식(ASAKyyyyMMddNNNNNN)의 중복되지 않는 orderNo를 생성한다.
     * ⑤ orders에 orderNo, orderTypeId, statusId, totalAmount를 INSERT하고 orderId를 받는다.
     * ⑥ 검증된 각 아이템을 order_item에 INSERT하고 orderItemId를 받는다.
     * ⑦ 각 아이템의 선택 옵션과 당시 옵션 가격을 order_item_option에 INSERT한다.
     * ⑧ 각 아이템의 제외재료를 item_exclusion에 INSERT한다.
     * ⑨ orderId, orderNo, totalAmount, RECEIVED만 CreateOrderResponse에 담아 반환한다.
     *
     * 주문 헤더부터 제외재료 저장까지 하나의 쓰기 트랜잭션으로 처리한다. 중간 INSERT가 하나라도 실패하면
     * 전체 주문이 롤백되어야 한다. 클라이언트 금액은 요청에 없으므로 totalAmount는 항상 서버 계산값을 사용한다.
     */

        // ------------ 주문 생성 api-005 ------------
        @Transactional
        public CreateOrderResponse createOrder(
                CreateOrderRequest request) {

            // 1. 요청 객체 확인
            // [피드백] request 자체를 먼저 차단하는 순서는 올바르다.
            // items가 null/빈 목록인지와 아이템 수량은 3단계의 공통 검증 메서드에서 처리한다.
            if (request == null) {
                throw new CustomException(
                        ErrorCode.INVALID_ORDER_REQUEST);
            }

            // 2. 주문 유형 확인
            // [피드백] CreateOrderRequest.orderType은 이미 OrderType enum이다.
            // 이 메서드는 현재 null 여부를 검증하며, 지원하지 않는 문자열은 JSON 역직렬화 단계에서 차단된다.
            validateOrderType(request.getOrderType());

            // 3. 메뉴·옵션·제외 재료 검증 및 서버 가격 계산 (공통 로직으로 확인)
            // [피드백] API-004와 동일한 검증 결과를 재사용하는 구조가 적절하다.
            // 클라이언트 금액을 받지 않고 DB 가격으로 totalAmount를 계산하는 것도 현재 명세와 맞다.
            ValidatedOrderResult result = validateAndPriceItems(
                    request.getItems());

            // 4. 공통 코드 조회
            // [피드백] orders에는 문자열 code가 아니라 common_code.id가 저장되므로 ID 조회가 필요하다.
            // 주문 유형과 주문 상태를 별도 Mapper로 분리하여 각 SQL에서 코드 그룹을 고정한 구조가 명확하다.
            // 주문 유형 text로 변환(orderTypeCode는 응답값이 아니라 DB 조회 조건)
            String orderTypeCode = request.getOrderType().name();

            // 주문 상태는 응답에서도 재사용하므로 OrderStatus enum으로 유지한다.
            OrderStatus initialStatus = OrderStatus.RECEIVED;

            // 주문 유형 확인
            Long orderTypeId = orderMapper.findOrderTypeId(
                    orderTypeCode);

            if (orderTypeId == null) {
                throw new CustomException(
                        ErrorCode.INVALID_ORDER_TYPE);
            }

            //주문 상태 확인
            Long statusId = orderMapper.findOrderStatusId(
                    initialStatus.name());

            if (statusId == null) {
                throw new CustomException(
                        ErrorCode.ORDER_INITIAL_STATUS_NOT_FOUND);
            }

            // 5. 주문번호 생성
            // [피드백] ASAK + yyyyMMdd + 일별 6자리 순번으로 조립한다.
            // 현재 구현은 기존 orders에서 마지막 번호를 읽는 방식이므로 order_no UNIQUE 제약과
            // 동시 요청의 중복 키 충돌 재시도 처리가 최종적으로 필요하다.
            String orderNo = generateOrderNo();

            // 6. 주문 헤더 저장
            // [피드백] 주문번호, 주문유형 ID, 초기상태 ID, 서버 계산 총액은 주문 한 건에 공통인 정보다.
            // useGeneratedKeys로 orders.id가 order.orderId에 채워져야 이후 주문 아이템을 연결할 수 있다.
            OrderInsertDto order = new OrderInsertDto();

            order.setOrderNo(orderNo);
            order.setOrderTypeId(orderTypeId);
            order.setStatusId(statusId);
            order.setTotalAmount(
            result.totalAmount());

            //주문 db orders 주문 내역 저장
            int insertedOrderCount = orderMapper.insertOrder(order);

            if (insertedOrderCount != 1
                    || order.getOrderId() == null) {

                throw new CustomException(
                        ErrorCode.ORDER_CREATE_FAILED);
            }

            //주문한 id 호출 및 저장
            Long orderId = order.getOrderId();

            // 7. 주문 아이템 저장
            // [피드백] 검증 완료 결과만 저장하는 방향이 올바르다.
            // 현재 OrderItemInsertDto와 insertOrderItem Mapper/XML은 아직 구현이 필요하다.
            for (ValidatedOrderItem item : result.items()) {

                OrderItemInsertDto orderItem = new OrderItemInsertDto();

                orderItem.setOrderId(orderId);
                orderItem.setMenuId(item.menuId());
                orderItem.setQuantity(item.quantity());
                orderItem.setUnitPrice(item.unitPrice());

                //저장된 orderItem의 행의 갯수 반환 받음
                int insertedItemCount = orderMapper.insertOrderItem(
                        orderItem);
                //orderItem이 제대로 저장되었는지 유무 확인 로직
                if (insertedItemCount != 1
                        || orderItem.getOrderItemId() == null) {

                    throw new CustomException(
                            ErrorCode.ORDER_ITEM_CREATE_FAILED);
                }

                //저장된 주문 아이템에 옵션과 제외 재료를 연결하기 위해 사용
                Long orderItemId = orderItem.getOrderItemId();

                // 8. 선택 옵션 저장
                // [피드백] 옵션은 주문 전체가 아니라 개별 order_item에 속하므로 orderItemId로 연결해야 한다.
                // 현재 insertOrderItemOption Mapper/XML은 아직 구현이 필요하다.
                for (ValidatedOptionItem option : item.optionItems()) {

                    int insertedOptionCount = orderMapper
                            .insertOrderItemOption(
                                    orderItemId,
                                    option.optionItemId(),
                                    option.quantity(),
                                    option.extraPrice());
                                    
                    //옵션 아이템 저장 실패시
                    if (insertedOptionCount != 1) {
                        throw new CustomException(
                                ErrorCode.ORDER_OPTION_CREATE_FAILED);
                    }
                }

                // 9. 제외 재료 저장
                // [피드백] 제외재료 역시 개별 order_item에 속하므로 orderItemId로 연결하는 것이 맞다.
                // 현재 insertItemExclusion Mapper/XML은 아직 구현이 필요하다.
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
            // [피드백] CreateOrderResponse는 @Builder 기반이므로 setter가 아니라 완성된 응답을 한 번에 만든다.
            // OrderStatus enum은 JSON 직렬화 시 최신 명세의 "RECEIVED" 문자열로 반환된다.
            return CreateOrderResponse.builder()
                    .orderId(orderId)
                    .orderNo(orderNo)
                    .totalAmount(result.totalAmount())
                    .status(initialStatus)
                    .build();
        }


        /**
         * 기존 orders 테이블을 기준 ASAKyyyyMMddNNNNNN 주문번호 생성
         */
        private String generateOrderNo() {

            // 1. 한국 시간 기준 오늘 날짜를 yyyyMMdd로 만든다.
            LocalDate orderDate = LocalDate.now(ORDER_ZONE_ID);
            String datePart = orderDate.format(ORDER_DATE_FORMATTER);

            // 2. 고정 접두사 ASAK와 날짜를 조합한다. 예: ASAK20250301
            String orderNoPrefix = ORDER_NO_PREFIX + datePart;

            // 3. orders에서 같은 날짜 접두사를 가진 가장 마지막 주문번호를 조회한다.
            String lastOrderNo = orderMapper.findLastOrderNoByPrefix(
                    orderNoPrefix);

            // 4. 오늘 첫 주문이면 1부터 시작한다.
            int nextSequence = 1;

            if (lastOrderNo != null) {
                // 저장된 주문번호가 정해진 18자리 형식을 벗어나면 잘못된 순번을 만들지 않고 중단한다.
                if (lastOrderNo.length() != orderNoPrefix.length() + 6
                        || !lastOrderNo.startsWith(orderNoPrefix)) {
                    throw new CustomException(
                            ErrorCode.ORDER_NUMBER_CREATE_FAILED);
                }

                try {
                    // 5. 마지막 주문번호의 뒤 6자리를 숫자로 변환하고 1을 더한다.
                    String lastSequencePart = lastOrderNo.substring(
                            orderNoPrefix.length());
                    nextSequence = Integer.parseInt(lastSequencePart) + 1;
                } catch (NumberFormatException exception) {
                    throw new CustomException(
                            ErrorCode.ORDER_NUMBER_CREATE_FAILED);
                }
            }

            // 6자리 순번의 최대값을 넘으면 같은 형식으로 더 이상 번호를 만들 수 없다.
            if (nextSequence > MAX_DAILY_ORDER_SEQUENCE) {
                throw new CustomException(
                        ErrorCode.ORDER_DAILY_SEQUENCE_EXCEEDED);
            }

            // 6. 다음 순번을 6자리로 채우고 최종 주문번호를 반환한다.
            return orderNoPrefix + String.format(
                    "%06d",
                    nextSequence);
        }

        //주문 형식 확인하는 메서드
        private void validateOrderType(OrderType orderType) {
            if (orderType == null) {
                throw new CustomException(
                        ErrorCode.INVALID_ORDER_TYPE);
            }
        }

}
