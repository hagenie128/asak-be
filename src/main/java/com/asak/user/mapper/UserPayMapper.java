package com.asak.user.mapper;

import com.asak.common.enums.PaymentMethod;
import com.asak.user.dto.payment.ApprovePaymentResponse;
import com.asak.user.dto.payment.PaymentMethodResponse;
import com.asak.user.dto.payment.command.PaymentInsertCommand;
import com.asak.user.dto.payment.query.PaymentIdempotencyCheck;
import com.asak.user.dto.payment.query.PaymentMethodContext;
import com.asak.user.dto.payment.query.PaymentOrderContext;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserPayMapper {

  // -------- api-014 결제 수단 조회 --------
  List<PaymentMethodResponse> findPaymentMethods();

  // -------- api-006 결제 승인 --------
  PaymentIdempotencyCheck findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

  ApprovePaymentResponse getPaymentResult(@Param("paymentId") Long paymentId);

  PaymentOrderContext findOrderForPayment(@Param("orderId") Long orderId);

  boolean existsApprovedPayment(@Param("orderId") Long orderId);

  PaymentMethodContext findPaymentMethod(
      @Param("paymentMethodCode") PaymentMethod paymentMethodCode);

  // 결제수단 저장
  int insertPayment(PaymentInsertCommand command);

  // orderStatus READY → RECEIVED 수정 반환
  int updateOrderStatusToReceived(@Param("orderId") Long orderId , @Param("watingDate") LocalDate watingDate , @Param("waitingOrderNo") Integer waitingOrderNo);

  //대기 고정 번호 +1 증감
  void increaseDailyWaitingSequence(@Param("watingDate") LocalDate watingDate);

  //동일 고정 대기번호 찾기
  int findDailyWaitingOrderNo(@Param("watingDate") LocalDate watingDate);
}
