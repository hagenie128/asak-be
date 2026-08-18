package com.asak.user.dto.order.internal;

// 검증된 옵션 아이템

public record ValidatedOptionItem(Long optionItemId, Long policyId, int quantity, int extraPrice) {}
