package com.asak.user.dto;

import java.math.BigDecimal;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 메뉴 영양정보

@Getter
@Setter
@Alias("Nutrition")
@NoArgsConstructor
public class Nutrition {

    private BigDecimal kcal;

}
