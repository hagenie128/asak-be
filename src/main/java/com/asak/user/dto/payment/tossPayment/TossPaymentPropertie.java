package com.asak.user.dto.payment.tossPayment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

// 토스페이먼츠 시크릿 키 설정 값

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "toss.payments")
public class TossPaymentPropertie {

    private String secretKey;
    private String baseUrl = "https://api.tosspayments.com";

}
