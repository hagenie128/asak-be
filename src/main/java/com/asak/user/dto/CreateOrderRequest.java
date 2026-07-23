package com.asak.user.dto;

import com.asak.common.enums.OrderType;
import java.util.List;
import lombok.Getter;

// 프론트앤드에서 받아온 request를 받는 그릇 역할
// ----[API정보 및 명세 작성]
// Endpoint: POST /api/kiosk/orders
// Request: orderType(TAKE_OUT/EAT_IN),
//         items[].{menuId, quantity,
//              optionItems[], excludedIngredientIds[]}
// Response data: orderId, orderNo, totalAmount, status(RECEIVED/PREPARING/COMPLETED)
//          — orderType, paymentStatus는 응답에 없음

@Getter
public class CreateOrderRequest {
  private OrderType orderType;
  private List<OrderItemRequest> items;
}
