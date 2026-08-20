package com.asak.admin.dto.response.dashboard;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardResponse {

  private String date;
  private List<Kpi> kpis;
  private List<RecentOrder> recentOrders;
  private List<CountSummary> statusSummary;
  private OrderTypeSummary orderTypeSummary;
  private List<InventoryAlert> inventoryAlerts;
  private List<WeeklySales> weeklySales;

  @Data @Builder public static class Kpi { private String label; private BigDecimal value; }
  @Data @Builder public static class RecentOrder {
    private String orderNo; private String orderType; private String menuSummary;
    private BigDecimal totalAmount; private String orderStatus; private String createdAtLabel;
  }
  @Data @Builder public static class CountSummary { private String label; private Long count; private String tone; }
  @Data @Builder public static class OrderTypeSummary { private Long eatIn; private Long takeOut; }
  @Data @Builder public static class InventoryAlert { private String label; private String badge; private String tone; }
  @Data @Builder public static class WeeklySales { private String label; private BigDecimal amount; }
}
