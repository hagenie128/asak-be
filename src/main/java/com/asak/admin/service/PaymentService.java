package com.asak.admin.service;

import com.asak.admin.dto.RefundTarget;
import com.asak.common.enums.PaymentMethod;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  public String cardRefund(RefundTarget target) {

    if (target.getPaymentMethod() != PaymentMethod.CARD) {
      throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED_FOR_REFUND);
    }

    if (target.getProviderPaymentKey() == null || target.getProviderPaymentKey().isBlank()) {

      throw new CustomException(ErrorCode.ORDER_REFUND_FAILED);
    }

    // 가상 카드 취소 성공
    return "VIRTUAL-CANCEL-" + UUID.randomUUID();
  }

  // public void kakaoPayRefund(RefundTarget target) throws CustomException {
  // if (!target.getPaymentMethod().name().equals(PaymentMethod.KAKAO_PAY.name()))
  // {
  // throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED_FOR_REFUND);
  // }

  // // 카카오페이 환불 성공
  // }

  // public void tossPayRefund(RefundTarget target) throws CustomException {
  // if (!target.getPaymentMethod().name().equals(PaymentMethod.TOSS_PAY.name()))
  // {
  // throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED_FOR_REFUND);
  // }

  // // 토스페이 환불 성공
  // }

  // public void naverPayRefund(RefundTarget target) throws CustomException {
  // if (!target.getPaymentMethod().name().equals(PaymentMethod.NAVER_PAY.name()))
  // {
  // throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED_FOR_REFUND);
  // }

  // // 네이버페이 환불 성공
  // }
}
