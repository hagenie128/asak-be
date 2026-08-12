package com.asak.user.mapper;

import org.apache.ibatis.annotations.Param;

import com.asak.common.enums.PaymentMethod;
import com.asak.user.dto.payment.ApprovePaymentResponse;
import com.asak.user.dto.payment.command.PaymentInsertCommand;
import com.asak.user.dto.payment.query.PaymentIdempotencyCheck;
import com.asak.user.dto.payment.query.PaymentMethodContext;
import com.asak.user.dto.payment.query.PaymentOrderContext;

public interface UserPayMapper {

    PaymentIdempotencyCheck findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    ApprovePaymentResponse getPaymentResult(@Param("paymentId") Long paymentId);

    PaymentOrderContext findOrderForPayment(@Param("orderId") Long orderId);

    boolean existsApprovedPayment(@Param("orderId") Long orderId);

    PaymentMethodContext findPaymentMethod(@Param("paymentMethodCode") PaymentMethod paymentMethodCode);

    int insertPayment(PaymentInsertCommand command);

}

