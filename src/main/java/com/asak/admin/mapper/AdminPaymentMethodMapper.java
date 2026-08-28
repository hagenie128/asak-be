package com.asak.admin.mapper;

import com.asak.admin.dto.response.AdminPaymentMethodResponse;
import java.util.List;
import java.util.Map;

// 결제수단 Mapper 계약 참고.
// 1) 목록은 pay_method_cfg와 common_code 기준으로 methodId, methodCode, methodName, imageUrl, description,
//    active, sortNo를 조회하고 sort_no ASC, id ASC를 적용한다.
// 2) 현재 PATCH 범위는 methodId 기준 active와 sortNo 한 행 수정이다. receiptMessage는 컬럼·DTO 계약 확정 전에는 추가하지 않는다.
// 3) UPDATE 변경 건수(0/1)는 Service가 대상 없음과 갱신 실패를 구분하는 근거로 사용한다.
public interface AdminPaymentMethodMapper {

  List<AdminPaymentMethodResponse> getPaymentMethods();

  int updatePaymentMethod(Map<String, Object> params);

  int findPaymentMethod(Long methodId);

  Long findPaymentMethodStatusId(String statusCode);

  AdminPaymentMethodResponse findPaymentMethodByCode(String methodCode);
}
