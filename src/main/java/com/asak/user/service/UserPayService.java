package com.asak.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asak.common.enums.OrderStatus;
import com.asak.common.enums.PaymentMethod;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.user.dto.payment.ApprovePaymentRequest;
import com.asak.user.dto.payment.ApprovePaymentResponse;
import com.asak.user.dto.payment.command.PaymentInsertCommand;
import com.asak.user.dto.payment.query.PaymentIdempotencyCheck;
import com.asak.user.dto.payment.query.PaymentMethodContext;
import com.asak.user.dto.payment.query.PaymentOrderContext;
import com.asak.user.mapper.UserPayMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPayService {

    private final UserPayMapper payMapper;

    // ------------ 결제 승인 검증 메서드 ------------
    private void validateRequest(ApprovePaymentRequest request){

        //1. 주문 요청 올바르지 않음
        if(request == null || 
            request.getOrderId() == null ||
            request.getIdempotencyKey() == null ){
                throw new CustomException(ErrorCode.INVALID_ORDER_REQUEST);
        }

        // 2. 결제 방법이 비활성화 되어있음
        if(request.getPaymentMethodCode() == null){
            throw new CustomException(ErrorCode.PAYMENT_METHOD_DISABLED);
        }

        // 3.이미 처리중인 결제가 있음
        if(request.getIdempotencyKey() == null ||
            request.getIdempotencyKey().isBlank()){
            throw new CustomException(ErrorCode.INVALID_ORDER_REQUEST);
        }

    }


     // ------------ 결제 멱등성키 중복 사용 확인 메서드 ------------
    private void validateSameRequest(PaymentIdempotencyCheck existing, ApprovePaymentRequest request){

        //true -> 중복 o(이미 사용한 멱등성 재사용) , false -> 중복 x
        boolean isDifferentRequest = !existing.getOrderId().equals(request.getOrderId()) || existing.getPaymentMethodCode() != request.getPaymentMethodCode();

        if(isDifferentRequest){
            // IDEMPOTENCY_KEY_CONFLICT : 이미 다른 결제 요청 사용된 멱등성키
            throw new CustomException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }


    }
    // ------------ 주문 존재 및 상태 검증 ------------
    private PaymentOrderContext validateOrderForPayment(Long orderId){
        
        PaymentOrderContext order = payMapper.findOrderForPayment(orderId);
        
        if(order == null){
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        if(order.getOrderStatus() != OrderStatus.RECEIVED){
            throw new CustomException(ErrorCode.ORDER_STATUS_CONFLICT);
        }
        
        return order;
        
    }
    
    // ------------ 기존 승인 결제 검증(이미 결제했는지 확인) ------------
    private void validateNoApprovePayment(Long orderId){
        
        boolean alreadyApproved = payMapper.existsApprovedPayment(orderId);
        
        if(alreadyApproved) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_APPROVED);
        }
        
    }
    
    // ------------ 결제수단 존재·활성화 여부 확인 ------------
    private PaymentMethodContext validateMethodForPayment(PaymentMethod paymentMethodCode) {
        
        PaymentMethodContext method = payMapper.findPaymentMethod(paymentMethodCode);
        
        if(method == null || !method.isEnable()){
            throw new CustomException(ErrorCode.PAYMENT_METHOD_DISABLED);
        }
        
        return method;
    }

    // ------------ getPaymentResult()가 null 일때 처리 ------------
    private ApprovePaymentResponse getRequiredPaymentResult(Long paymentId){

        ApprovePaymentResponse result = payMapper.getPaymentResult(paymentId);

        if(result == null){
            throw new CustomException(ErrorCode.PAYMENT_CREATE_FAILED);
        }

        return result;

    }


    //requestBody 정본
    //     {
    // "orderId": 1,
    // "paymentMethodCode": "CARD",
    // "idempotencyKey": "uuid"
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
    public ApprovePaymentResponse createApprovePayment(ApprovePaymentRequest request){

        // 1. 요청 형식 검증(request 확인)
        validateRequest(request);

        // 2. idempotencyKey로 기존 결제 조회
        //    - 있으면 기존 결과 반환 또는 키 재사용 오류
        //    - existing : 과거에 어떤 결제 요청에 이미 사용된 것.
        PaymentIdempotencyCheck existing = payMapper.findByIdempotencyKey(request.getIdempotencyKey());

        if(existing != null){
            //같은 키가 다른 요청에 재사용 됐는지 검증(결과값 false가 나와야함)
            validateSameRequest(existing, request);

            return getRequiredPaymentResult(existing.getPaymentId());
        }

        // 3. 주문 존재 및 주문 상태 확인
        PaymentOrderContext order = validateOrderForPayment(request.getOrderId());

        // 4. 해당 주문의 기존 APPROVED(승인) 결제 확인
        validateNoApprovePayment(request.getOrderId());

        // 5. 결제수단 존재·활성화 여부 확인

        PaymentMethodContext paymethod = validateMethodForPayment(request.getPaymentMethodCode());

        // 6. orders.total_price를 승인 금액으로 결정

        int approvedAmount = order.getTotalPrice();
        
        // 7. payment 저장
        PaymentInsertCommand command = new PaymentInsertCommand();

        command.setOrderId(request.getOrderId());
        command.setMethodId(paymethod.getMethodId());
        command.setAmount(approvedAmount);
        command.setIdempotencyKey(request.getIdempotencyKey());

        int inserted = payMapper.insertPayment(command); //건수 -> 추가성공  0-> 실패

        if(inserted != 1 || command.getPaymentId() == null){
            throw new CustomException(ErrorCode.PAYMENT_CREATE_FAILED);
        }

        // 8. paymentId로 결과 조회 후 반환
        return getRequiredPaymentResult(command.getPaymentId());
    }




}
