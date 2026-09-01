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

    // 카드 단말 결제는 현재 가상 취소만 지원한다. 실제 PG 연동 시 providerPaymentKey 검증과
    // PG 취소 호출을 이 지점에 추가한다.
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
