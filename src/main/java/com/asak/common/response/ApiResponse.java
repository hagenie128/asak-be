package com.asak.common.response;

import lombok.Builder;
import lombok.Getter;

// -- [응답 공통 Api] --
// {
//   "success": true,
//   "status": 200,
//   "code": "SUCCESS",
//   "message": "OK",
//   "data": {...
//   }
// }

//모든 API의 반환 규격
// ** T는 제네릭(Generic)

@Getter
@Builder
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String code;
    private String message;
    private T data;


}
