package com.asak.user.mapper;

import com.asak.user.dto.query.MenuQueryDTO;
import com.asak.user.dto.query.OptionItemQueryDTO;

public interface UserOrderMapper {

    
    MenuQueryDTO selectByMenuId(Long menuId);

    OptionItemQueryDTO findByOptionItem(Long menuId, Long optionItemId);}
