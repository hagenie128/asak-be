package com.asak.admin.controller;

import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-027 [구현 완료 · API 검증 대기]: POST /api/admin/login은 { storeNumber }만 받고,
// 고정값 "0001"의 승인 결과(approved)만 반환한다. 계정/비밀번호·JWT·DB 매장 조회는 이번 범위가 아니다.
// TODO-031/032/034는 approved 결과와 기존 loggedIn 세션 경계를 사용한다.
// "0001"·잘못된 매장 번호·빈 입력은 각각 성공/400/400으로 구현돼 있으므로 실제 API 응답을 확인해야 한다.
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

  @PostMapping("/login")
  public ApiResponse<Map<String, Boolean>> login(@RequestBody Map<String, String> request) {
    String storeNumber = request.get("storeNumber");
    if (storeNumber == null || storeNumber.isEmpty()) {
      return ApiResponse.error(ErrorCode.INVALID_STORE_NUMBER);
    } else if (storeNumber.equals("0001")) {
      return ApiResponse.success("ADMIN_LOGIN_SUCCESS", "로그인 성공", Map.of("approved", true));
    } else {
      return ApiResponse.error(ErrorCode.NOT_APPROVED_STORE_NUMBER);
    }
  }
}
