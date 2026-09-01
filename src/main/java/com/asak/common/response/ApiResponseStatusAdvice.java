package com.asak.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** ApiResponse.error()의 status 필드와 실제 HTTP 상태를 일치시킨다. */
@RestControllerAdvice
public class ApiResponseStatusAdvice implements ResponseBodyAdvice<ApiResponse<?>> {

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
  }

  @Override
  public ApiResponse<?> beforeBodyWrite(
      ApiResponse<?> body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body != null && !body.isSuccess()) {
      response.setStatusCode(HttpStatusCode.valueOf(body.getStatus()));
    }
    return body;
  }
}
