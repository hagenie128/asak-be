package com.asak.admin.dto.response;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CategoryListResponse
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Alias("adminCategory")
public class AdminCategoryResponse {
    private Long categoryId;
    private String categoryName;
    private Integer categorySortNo;
    private Boolean categoryActive;
}
