package com.asak.common.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매출 API의 날짜·기간 파라미터가 빠지거나 형식이 어긋날 때 500이 아니라 400이 나가는지 확인한다. 실제 컨트롤러 대신 같은 형태의 파라미터를 받는 최소 컨트롤러를
 * 세워 advice만 검증한다.
 */
class GlobalExceptionHandlerTest {

  @RestController
  static class ProbeController {
    // /api/admin/sales/monthly 와 같은 형태 (year 필수 int)
    @GetMapping("/probe/monthly")
    String monthly(@RequestParam int year) {
      return "ok";
    }
  }

  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(new ProbeController())
          .setControllerAdvice(new GlobalExceptionHandler())
          .build();

  @Test
  @DisplayName("필수 파라미터가 없으면 400과 파라미터명을 반환한다")
  void missingParameterReturnsBadRequest() throws Exception {
    mvc.perform(get("/probe/monthly"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
        .andExpect(jsonPath("$.message").value(containsString("year")));
  }

  @Test
  @DisplayName("파라미터 형식이 어긋나면 400을 반환한다")
  void typeMismatchReturnsBadRequest() throws Exception {
    mvc.perform(get("/probe/monthly").param("year", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"))
        .andExpect(jsonPath("$.message").value(containsString("year")));
  }

  @Test
  @DisplayName("정상 요청은 그대로 통과한다")
  void validRequestPasses() throws Exception {
    mvc.perform(get("/probe/monthly").param("year", "2026")).andExpect(status().isOk());
  }
}
