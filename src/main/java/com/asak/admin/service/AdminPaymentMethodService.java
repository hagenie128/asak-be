package com.asak.admin.service;

import com.asak.admin.dto.request.UpdatePaymentMethodRequest;
import com.asak.admin.dto.response.AdminPaymentMethodResponse;
import com.asak.admin.mapper.AdminPaymentMethodMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// TODO-012: 결제수단 2/4 — Service/Mapper/DTO 구현.
// 1) 목록 조회(getPaymentMethods)와 PATCH 대상(patchPaymentMethod)을 추가하고 DTO를 Entity/Mapper 결과와 분리한다.
// 2) 활성/정렬/영수증문구 validation, 존재하지 않는 id, 0건 update, 정렬 충돌 규칙을 Controller ErrorCode와 맞춘다.
// 3) 순서 변경이 여러 행을 바꾸면 transaction 범위와 동시 변경 정책을 정한 뒤 TODO-011 Controller와 TODO-013 프런트를 연결한다.
@Service
public class AdminPaymentMethodService {

  private final AdminPaymentMethodMapper adminPaymentMethodMapper;

  public AdminPaymentMethodService(AdminPaymentMethodMapper adminPaymentMethodMapper) {
    this.adminPaymentMethodMapper = adminPaymentMethodMapper;
  }

  public List<AdminPaymentMethodResponse> getPaymentMethods() {
    return adminPaymentMethodMapper.getPaymentMethods();
  }

  public int updatePaymentMethod(Long paymentMethodId, UpdatePaymentMethodRequest request) {
    Map<String, Object> params = new HashMap<>();
    params.put("id", paymentMethodId);
    params.put("active", request.getActive());
    params.put("sortNo", request.getSortNo());
    return adminPaymentMethodMapper.updatePaymentMethod(params);
  }
}
