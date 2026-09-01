package com.asak.admin.service;

import com.asak.admin.dto.response.orders.RefundReasonResponse;
import com.asak.admin.mapper.AdminCommonCodeMapper;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminRefundReasonService {

  static final String REFUND_REASON_GROUP = "REFUND_REASON";
  static final String OTHER_CODE = "OTHER";

  private final AdminCommonCodeMapper adminCommonCodeMapper;

  public AdminRefundReasonService(AdminCommonCodeMapper adminCommonCodeMapper) {
    this.adminCommonCodeMapper = adminCommonCodeMapper;
  }

  public List<RefundReasonResponse> getRefundReasons() {
    return adminCommonCodeMapper.findActiveCodesByGroup(REFUND_REASON_GROUP);
  }

  public String resolveRefundReasonText(String refundReasonCode, String refundReasonDetail) {
    if (!StringUtils.hasText(refundReasonCode)) {
      throw new CustomException(ErrorCode.INVALID_REFUND_REASON);
    }

    RefundReasonResponse reason =
        adminCommonCodeMapper.findActiveCodeByGroupAndCode(
            REFUND_REASON_GROUP, refundReasonCode.trim());

    if (reason == null) {
      throw new CustomException(ErrorCode.INVALID_REFUND_REASON);
    }

    if (OTHER_CODE.equals(reason.getCode())) {
      if (!StringUtils.hasText(refundReasonDetail)) {
        throw new CustomException(ErrorCode.REFUND_REASON_DETAIL_REQUIRED);
      }
      return refundReasonDetail.trim();
    }

    return reason.getName();
  }
}
