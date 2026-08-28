package com.asak.user.service;

import com.asak.common.enums.OrderStatus;
import com.asak.common.enums.PaymentMethod;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.user.dto.payment.ApprovePaymentRequest;
import com.asak.user.dto.payment.ApprovePaymentResponse;
import com.asak.user.dto.payment.PaymentMethodListResponse;
import com.asak.user.dto.payment.PaymentMethodResponse;
import com.asak.user.dto.payment.command.PaymentInsertCommand;
import com.asak.user.dto.payment.query.PaymentIdempotencyCheck;
import com.asak.user.dto.payment.query.PaymentMethodContext;
import com.asak.user.dto.payment.query.PaymentOrderContext;
import com.asak.user.dto.payment.tossPayment.TossPaymentAuth;
import com.asak.user.dto.payment.tossPayment.TossPaymentClient;
import com.asak.user.dto.payment.tossPayment.TossPaymentConfirmRequest;
import com.asak.user.dto.payment.tossPayment.TossPaymentConfirmResponse;
import com.asak.user.mapper.UserPayMapper;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPayService {

  private final UserPayMapper payMapper;
  private final TossPaymentClient tossPaymentClient;

  // --------------- api-014 결제 수단 조회 ------------------------
  public PaymentMethodListResponse getPaymentMethod() {

    List<PaymentMethodResponse> methodList = payMapper.findPaymentMethods();

    PaymentMethodListResponse response = new PaymentMethodListResponse();

    response.setMethods(methodList);

    return response;
  }

  // ------------ 결제 승인 검증 메서드 ------------
  private void validateRequest(ApprovePaymentRequest request) {

    // 1. 주문 요청 올바르지 않음
    if (request == null
        || request.getOrderId() == null
        || request.getOrderStatus() == null
        || request.getIdempotencyKey() == null) {
      throw new CustomException(ErrorCode.INVALID_ORDER_REQUEST);
    }

    // 2. 결제 방법이 비활성화 되어있음
    if (request.getPaymentMethodCode() == null) {
      throw new CustomException(ErrorCode.PAYMENT_METHOD_DISABLED);
    }

    // 3.이미 처리중인 결제가 있음
    if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
      throw new CustomException(ErrorCode.INVALID_ORDER_REQUEST);
    }

    // 4. 결제 요청은 RECEIVED 주문에 대해서만 허용
    if (request.getOrderStatus() != OrderStatus.RECEIVED) {
      throw new CustomException(ErrorCode.ORDER_STATUS_CONFLICT);
    }

    /*
     * 토스를 사용하는 결제수단은
     * 프론트에서 받은 토스 인증 결과가 필수
     */
    if (isTossEasyPay(request.getPaymentMethodCode())) {
      TossPaymentAuth tossPayment = request.getTossPayment();

      if (tossPayment == null
          || tossPayment.getPaymentKey() == null
          || tossPayment.getPaymentKey().isBlank()
          || tossPayment.getOrderId() == null
          || tossPayment.getOrderId().isBlank()
          || tossPayment.getAmount() == null
          || tossPayment.getAmount() <= 0) {
        throw new CustomException(ErrorCode.TOSS_PAYMENT_AUTH_REQUIRED);
      }
    }

    /*
     * CARD는 토스 인증 데이터를 사용하지 않음
     * 잘못 섞인 요청도 차단하려면 아래 검증을 추가
     */
    if (request.getPaymentMethodCode() == PaymentMethod.CARD && request.getTossPayment() != null) {
      throw new CustomException(ErrorCode.INVALID_PAYMENT_REQUEST);
    }
  }

  // ------------ 결제 승인 검증 메서드 ------------
  private void validateTossPaymentMethodsOrder(
      PaymentOrderContext order, ApprovePaymentRequest request) {

    if (!isTossEasyPay(request.getPaymentMethodCode())) {
      return;
    }

    TossPaymentAuth tossPayment = request.getTossPayment();

    if (!order.getOrderNo().equals(tossPayment.getOrderId())) {
      throw new CustomException(ErrorCode.TOSS_ORDER_ID_MISMATCH);
    }

    if (order.getTotalPrice() != tossPayment.getAmount()) {
      throw new CustomException(ErrorCode.TOSS_PAYMENT_AMOUNT_MISMATCH);
    }
  }

  // ------------ 결제 멱등성키 중복 사용 확인 메서드 ------------
  private void validateSameRequest(
      PaymentIdempotencyCheck existing, ApprovePaymentRequest request) {

    // true -> 중복 o(이미 사용한 멱등성 재사용) , false -> 중복 x
    boolean isDifferentRequest =
        !existing.getOrderId().equals(request.getOrderId())
            || existing.getPaymentMethodCode() != request.getPaymentMethodCode();

    if (isDifferentRequest) {
      // IDEMPOTENCY_KEY_CONFLICT : 이미 다른 결제 요청 사용된 멱등성키
      throw new CustomException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
    }
  }

  // ------------ 주문 존재 및 상태 검증 ------------
  private PaymentOrderContext validateOrderForPayment(Long orderId) {

    PaymentOrderContext order = payMapper.findOrderForPayment(orderId);

    if (order == null) {
      throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
    }

    if (order.getOrderStatus() != OrderStatus.READY) {
      throw new CustomException(ErrorCode.ORDER_STATUS_CONFLICT);
    }

    return order;
  }

  // ------------ 기존 승인 결제 검증(이미 결제했는지 확인) ------------
  private void validateNoApprovePayment(Long orderId) {

    boolean alreadyApproved = payMapper.existsApprovedPayment(orderId);

    if (alreadyApproved) {
      throw new CustomException(ErrorCode.PAYMENT_ALREADY_APPROVED);
    }
  }

  // ------------ 결제수단 존재·활성화 여부 확인 ------------
  private PaymentMethodContext validateMethodForPayment(PaymentMethod paymentMethodCode) {

    PaymentMethodContext method = payMapper.findPaymentMethod(paymentMethodCode);

    if (method == null || !method.isEnable()) {
      throw new CustomException(ErrorCode.PAYMENT_METHOD_DISABLED);
    }

    return method;
  }

  // ------------ getPaymentResult()가 null 일때 처리 ------------
  private ApprovePaymentResponse getRequiredPaymentResult(Long paymentId) {

    ApprovePaymentResponse result = payMapper.getPaymentResult(paymentId);

    if (result == null) {
      throw new CustomException(ErrorCode.PAYMENT_CREATE_FAILED);
    }

    return result;
  }

  // ------------ 토스 결제수단 판별 추가 ------------
  private boolean isTossEasyPay(PaymentMethod paymentMethod) {
    return paymentMethod == PaymentMethod.TOSS_PAY
        || paymentMethod == PaymentMethod.KAKAO_PAY
        || paymentMethod == PaymentMethod.NAVER_PAY;
  }

  // ------------ 토스 승인 성공 여부를 검증 ------------
  private void validateTossApprovalResponse(
      TossPaymentConfirmResponse tossResponse, TossPaymentConfirmRequest tossConfirmrRequest) {

    // 토스페이먼츠로부터 승인 거절된 경우
    if (tossResponse == null || !"DONE".equals(tossResponse.getStatus())) {
      throw new CustomException(ErrorCode.TOSS_PAYMENT_APPROVAL_FAILED);
    }

    // 토스페이먼츠로부터 PaymentKey값이 다른 경우
    if (!tossConfirmrRequest.getPaymentKey().equals(tossResponse.getPaymentKey())) {
      throw new CustomException(ErrorCode.TOSS_PAYMENT_KEY_MISMATCH);
    }

    // 토스페이먼츠응답과 요청의 OrderId값이 다른 경우
    if (!tossConfirmrRequest.getOrderId().equals(tossResponse.getOrderId())) {
      throw new CustomException(ErrorCode.TOSS_ORDER_ID_MISMATCH);
    }

    // 토스페이먼츠응답과 요청의 TotalAmount값이 다른 경우
    if (tossResponse.getTotalAmount() == null
        || tossConfirmrRequest.getAmount().longValue()
            != tossResponse.getTotalAmount().longValue()) {
      throw new CustomException(ErrorCode.TOSS_PAYMENT_AMOUNT_MISMATCH);
    }

    if (tossResponse.getApprovedAt() == null) {
      throw new CustomException(ErrorCode.TOSS_PAYMENT_APPROVAL_FAILED);
    }
  }

  // ------------ 토스 통신 예외의 원인이 연결·응답 시간 초과인지 확인 ------------
  private boolean isTimeout(ResourceAccessException exception) {
    Throwable cause = exception;

    while (cause != null) {
      if (cause instanceof SocketTimeoutException
          || cause instanceof HttpTimeoutException
          || cause instanceof TimeoutException) {
        return true;
      }
      cause = cause.getCause();
    }

    return false;
  }

  // requestBody 정본
  // --토스페이 반영 x --
  // {"orderId": 1, "paymentMethodCode": "CARD", "idempotencyKey": "uuid",
  // "orderStatus": "RECEIVED"}

  // --토스페이반영 o --
  // {
  //   "orderId": 1,
  //   "orderStatus": "RECEIVED",
  //   "paymentMethodCode": "TOSS_PAY",
  //   "idempotencyKey": "uuid",
  //   "tossPayment": {
  //     "paymentKey": "tgen_...",
  //     "orderId": "A202607230001",
  //     "amount": 8900
  //   }
  // }
  //     {
  //   "success": true,
  //   "status": 200,
  //   "code": "KIOSK_PAYMENT_APPROVED",
  //   "message": "결제가 승인되었습니다.",
  //   "data": {
  //     "paymentId": 1,
  //     "orderId": 1,
  //     "orderNo": "A202607230001",
  //     "paymentStatus": "APPROVED",
  //     "approvedAmount": 8900,
  //     "approvedAt": "2026-07-23T12:00:00",
  //     "waitingOrderCount": 0
  //   }
  // }

  // ------------ 결제 승인 api-006 ------------
  @Transactional
  public ApprovePaymentResponse createApprovePayment(ApprovePaymentRequest request) {

    // 1. 요청 형식 검증(request 확인)
    validateRequest(request);

    // 2. idempotencyKey로 기존 결제 조회
    //    - 있으면 기존 결과 반환 또는 키 재사용 오류
    //    - existing : 과거에 어떤 결제 요청에 이미 사용된 것.
    PaymentIdempotencyCheck existing = payMapper.findByIdempotencyKey(request.getIdempotencyKey());

    if (existing != null) {
      // 같은 키가 다른 요청에 재사용 됐는지 검증(결과값 false가 나와야함)
      validateSameRequest(existing, request);

      return getRequiredPaymentResult(existing.getPaymentId());
    }

    // 3. 주문 존재 및 주문 상태 확인
    PaymentOrderContext order = validateOrderForPayment(request.getOrderId());

    // 3-1. 토스페이먼츠로 추가되는 부분(토스 요청값과 DB주문번호와 금액 비교)
    validateTossPaymentMethodsOrder(order, request);

    // 4. 해당 주문의 기존 APPROVED(승인) 결제 확인
    validateNoApprovePayment(request.getOrderId());

    // 5. 결제수단 존재·활성화 여부 확인

    PaymentMethodContext paymethod = validateMethodForPayment(request.getPaymentMethodCode());

    // 6. orders.total_price를 승인 금액으로 결정

    int approvedAmount = order.getTotalPrice();

    String providerPaymentKey = null;
    OffsetDateTime approvedAt = null;

    // 6-1. 토스페이 간편결제 내역들만 해당 구간 적용(결제수단:card 제외)
    if (isTossEasyPay(request.getPaymentMethodCode())) {

      // 6-2. 토스페이먼츠로 api 보내주는 작업 실행
      TossPaymentAuth tossPayment = request.getTossPayment();

      TossPaymentConfirmRequest tossConfirmRequest = new TossPaymentConfirmRequest();

      tossConfirmRequest.setPaymentKey(tossPayment.getPaymentKey());
      tossConfirmRequest.setOrderId(order.getOrderNo());
      tossConfirmRequest.setAmount((long) order.getTotalPrice());

      // 6-3. 토스페이먼츠로부터 에러 발생했을시의 예외처리
      try {
        TossPaymentConfirmResponse tossResponse =
            tossPaymentClient.confirm(tossConfirmRequest, request.getIdempotencyKey());

        // 6-2. 토스페이로부터 온 응답을 검증
        validateTossApprovalResponse(tossResponse, tossConfirmRequest);

        providerPaymentKey = tossResponse.getPaymentKey();
        approvedAt = tossResponse.getApprovedAt();

      } catch (ResourceAccessException e) {
        if (isTimeout(e)) {
          throw new CustomException(ErrorCode.PAYMENT_TIMEOUT);
        }
        throw new CustomException(ErrorCode.PAYMENT_NETWORK_ERROR);
      }
    }

    // 7. payment 저장
    PaymentInsertCommand command = new PaymentInsertCommand();

    command.setOrderId(request.getOrderId());
    command.setMethodId(paymethod.getMethodId());
    command.setAmount(approvedAmount);
    command.setIdempotencyKey(request.getIdempotencyKey());

    // 토스페이먼츠 결제일 경우에만 저장
    if (providerPaymentKey != null && approvedAt != null) {
      command.setProviderPaymentKey(providerPaymentKey);
      command.setApprovedAt(approvedAt);
    }

    int inserted = payMapper.insertPayment(command); // 건수 -> 추가성공  0-> 실패

    if (inserted != 1 || command.getPaymentId() == null) {
      throw new CustomException(ErrorCode.PAYMENT_CREATE_FAILED);
    }
    // 주문상태(orderStatus)도 READY → RECEIVED로 바꿔서 보내주기
    int updated = payMapper.updateOrderStatusToReceived(request.getOrderId());

    if (updated != 1) {
      throw new CustomException(ErrorCode.ORDER_STATUS_CONFLICT);
    }

    // 8. paymentId로 결과 조회 후 반환
    return getRequiredPaymentResult(command.getPaymentId());
  }
}
