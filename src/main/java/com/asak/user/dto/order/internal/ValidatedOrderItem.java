package com.asak.user.dto.order.internal;

import java.util.List;

// <검증된 주문 아이템>
public record ValidatedOrderItem(
        Long menuId,
        int quantity,
        int unitPrice,
        List<ValidatedOptionItem> optionItems,
        List<Long> excludedIngredientIds
) {

}
