package com.asak.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuListRequest {

  private Long categoryId;
  private String keyword = "";
  private Boolean isSoldOut;
  private Long tagId;

  @Min(0)
  private int page = 0;

  @Min(1)
  @Max(100)
  private int size = 20;

  private String sort = "name,asc";

  public int getOffset() {
    return page * size;
  }
}
