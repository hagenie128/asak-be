package com.asak.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.asak.user.dto.order.query.MenuQueryDto;
import com.asak.user.dto.order.query.OptionItemQueryDto;
import com.asak.user.dto.order.query.OptionPolicyQueryDto;

public interface UserOrderMapper {

    
    MenuQueryDto selectByMenuId(Long menuId);

    OptionItemQueryDto findByOptionItem(@Param("menuId") Long menuId, @Param("optionItemId") Long optionItemId);

    List<OptionPolicyQueryDto> findOptionPoliciesByMenuId(Long menuId);

    Long findRemovableIngredient(@Param("menuId")Long menuId, @Param("ingredientId") Long ingredientId);}
