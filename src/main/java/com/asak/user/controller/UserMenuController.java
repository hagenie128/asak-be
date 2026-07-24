package com.asak.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.common.response.ApiResponse;
import com.asak.user.dto.CategoryResponse;
import com.asak.user.service.UserMenuService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/*
* RestController 에서는 요청별 이노테이션을 적용시켜줘야함
*
* GetMapping -> 조회
* PostMapping -> 추가
* put -> 데이터 전체 수정
* patch -> 데이터 부분 수정
* delete -> 삭제
*
* */

// ---[메뉴]---
// 카테고리 조회
// 메뉴 목록 조회
// 메뉴 상세 조회

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class UserMenuController {

    //메뉴 서비스 연결
    private final UserMenuService menuService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {

        List<CategoryResponse> categories = menuService.selectCategory();

        return ApiResponse.success(categories);
    }
    

    





}
