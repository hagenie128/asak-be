package com.asak.user.mapper;

import org.apache.ibatis.annotations.Param;

import com.asak.user.dto.query.MenuQueryDTO;
import com.asak.user.dto.query.OptionItemQueryDTO;

public interface UserOrderMapper {

    
    MenuQueryDTO selectByMenuId(Long menuId);

    OptionItemQueryDTO findByOptionItem(@Param("menuId") Long menuId, @Param("optionItemId") Long optionItemId);

    Long findRemovableIngredient(@Param("menuId")Long menuId, @Param("ingredientId") Long ingredientId);}
