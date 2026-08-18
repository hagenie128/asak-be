package com.asak.common.response;

import java.util.List;
import lombok.Getter;

@Getter
public class PageResult<T> {

  private final List<T> content; // 현재 페이지의 데이터 리스트
  private final int page; // 현재 페이지 번호 (0부터 시작)
  private final int size; // 페이지당 데이터 개수
  private final long totalElements; // 전체 데이터 개수
  private final int totalPages; // 전체 페이지 개수

  public PageResult(List<T> content, int page, int size, long totalElements) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
  }
}
