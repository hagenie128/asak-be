package com.asak.user.dto.order.request;

import java.util.List;

public interface OrderItemCommand {

  Long getMenuId();

  Integer getQuantity();

  List<OptionItemRequest> getOptionItems();

  List<Long> getExcludedIngredientIds();
}
