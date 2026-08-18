package com.asak.user.dto.order.internal;

import java.util.List;

// 장바구니 & 주문 <전체 검증 결과>
public record ValidatedOrderResult(int totalAmount, List<ValidatedOrderItem> items) {}
