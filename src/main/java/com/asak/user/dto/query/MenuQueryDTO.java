package com.asak.user.dto.query;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Service가 DB에서 메뉴 검증/계산할 때 사용

@Getter
@Setter
@NoArgsConstructor
@Alias("menuQueryDTO")
public class MenuQueryDTO {

    private Long menuId;
    private Integer price;
    private boolean isSoldOut;

}
