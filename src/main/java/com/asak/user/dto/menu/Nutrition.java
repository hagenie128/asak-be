package com.asak.user.dto.menu;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 메뉴 영양정보

@Getter
@Setter
@Alias("Nutrition")
@NoArgsConstructor
public class Nutrition {

  private BigDecimal kcal;
}
