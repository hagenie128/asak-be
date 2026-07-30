package com.asak.user.dto.order.query;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Service가 DB에서 메뉴 검증/계산할 때 사용

@Getter
@Setter
@NoArgsConstructor
@Alias("menuQueryDto")
public class MenuQueryDto {

    private Long menuId;
    private Integer price;
    private boolean isSoldOut;

}
