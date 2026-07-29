package com.asak.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.asak.user.dto.query.MenuQueryDTO;
import com.asak.user.dto.query.OptionItemQueryDTO;
import com.asak.user.dto.query.OptionPolicyQueryDTO;

public interface UserOrderMapper {

    
    MenuQueryDTO selectByMenuId(Long menuId);

    OptionItemQueryDTO findByOptionItem(@Param("menuId") Long menuId, @Param("optionItemId") Long optionItemId);

    List<OptionPolicyQueryDTO> findOptionPoliciesByMenuId(Long menuId);

    Long findRemovableIngredient(@Param("menuId")Long menuId, @Param("ingredientId") Long ingredientId);}
