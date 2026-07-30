package com.asak.user.dto.order.query;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메뉴별 옵션 정책의 선택 조건을 검증하기 위한 조회 DTO. */
@Getter
@Setter
@NoArgsConstructor
@Alias("optionPolicyQueryDto")
public class OptionPolicyQueryDto {

    private Long policyId;
    private Integer minSelect;
    private Integer maxSelect;
    private Boolean isRequired;
}
