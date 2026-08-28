package com.asak.admin.mapper;

import com.asak.admin.dto.response.orders.RefundReasonResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AdminCommonCodeMapper {

  List<RefundReasonResponse> findActiveCodesByGroup(@Param("groupCode") String groupCode);

  RefundReasonResponse findActiveCodeByGroupAndCode(
      @Param("groupCode") String groupCode, @Param("code") String code);
}
