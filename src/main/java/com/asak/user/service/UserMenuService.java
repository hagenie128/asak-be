package com.asak.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asak.user.dto.CategoryResponse;
import com.asak.user.mapper.UserMenuMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMenuService {

    private final UserMenuMapper menuMapper;


    public List<CategoryResponse> selectCategory() {
        return menuMapper.selectCategory();
    }
}



