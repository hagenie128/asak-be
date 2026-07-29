package com.asak.user.dto.query;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Service가 DB에서 메뉴 옵션에 대한 검증/계산할 때 사용

@Getter
@Setter
@NoArgsConstructor
@Alias("optionItemQueryDTO")
public class OptionItemQueryDTO {

    private Long policyId;
    private Long optionItemId;
    private Integer extraPrice;
    private Boolean isSoldOut;

}
