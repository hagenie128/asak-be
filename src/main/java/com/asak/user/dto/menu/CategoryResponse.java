package com.asak.user.dto.menu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("category")
public class CategoryResponse {

  private Long categoryId;
  private String categoryName;
  private Integer sortOrder;
  private Boolean isActive;
}
