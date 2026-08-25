package com.asak.admin.service;

import com.asak.admin.dto.request.UpdatePaymentMethodRequest;
import com.asak.admin.dto.response.AdminPaymentMethodResponse;
import com.asak.admin.mapper.AdminPaymentMethodMapper;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// TODO-012 [구현 완료 · SQL/실 DB 검증 대기]: 목록과 수정은 DTO로 분리했고,
// PATCH 식별자는 Controller path variable만 사용한다. active·sortNo와 없는 id는 검증한다.
// 목록 SQL은 sort_no ASC, id ASC 정렬을 보장해야 한다. 현재 SQL의 ORDER BY 및 실제 DB 결과를 확인한 뒤
// 정렬 완료로 표시한다. 여러 행 재정렬의 충돌 정책·transaction은 별도 범위다.
@Service
public class AdminPaymentMethodService {

  private final AdminPaymentMethodMapper adminPaymentMethodMapper;

  public AdminPaymentMethodService(AdminPaymentMethodMapper adminPaymentMethodMapper) {
    this.adminPaymentMethodMapper = adminPaymentMethodMapper;
  }

  public List<AdminPaymentMethodResponse> getPaymentMethods() {
    return adminPaymentMethodMapper.getPaymentMethods();
  }

  public int updatePaymentMethod(Long methodId, UpdatePaymentMethodRequest request) {
    if (methodId == null || adminPaymentMethodMapper.findPaymentMethod(methodId) == 0) {
      throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
    }
    if (request.getActive() == null || request.getSortNo() == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
    Map<String, Object> params = new HashMap<>();
    params.put("id", methodId);
    params.put("active", request.getActive());
    params.put("sortNo", request.getSortNo());
    return adminPaymentMethodMapper.updatePaymentMethod(params);
  }
}
