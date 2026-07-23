package com.asak.user.dto;

import java.util.List;
import com.asak.user.dto.OptionItemRequest;
import lombok.Getter;

@Getter
public class OrderItemRequest {

    private Long menuId;
    private Integer quantity;
    private List<OptionItemRequest> optionItems;
    private List<Long> excludedIngredientIds;

}
