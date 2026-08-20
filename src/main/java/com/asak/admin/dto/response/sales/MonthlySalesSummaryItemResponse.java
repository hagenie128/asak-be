package com.asak.admin.dto.response.sales;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 월별 매출 요약 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesSummaryItemResponse {

  private int year;

  private int month;

  /** 순매출 */
  private BigDecimal totalSales;

  /** 정상 주문 건수 */
  private Long totalOrders;

  /** 취소/환불 주문 건수 */
  private Long canceledOrders;

  /** 총 결제 금액 */
  private BigDecimal grossSalesAmount;

  /** 취소/환불 금액 */
  private BigDecimal canceledAmount;

  /** 객단가 */
  private BigDecimal averageOrderAmount;

  /** 취소율 (%) */
  private BigDecimal cancelRate;

  /** 구매 고객 수 */
  private Long totalCustomers;

  /** 판매 상품 수 */
  private Long totalProducts;
}
