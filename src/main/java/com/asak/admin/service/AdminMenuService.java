package com.asak.admin.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.asak.admin.dto.request.CreateMenuIngredientRequest;
import com.asak.admin.dto.request.CreateMenuOptionGroupRequest;
import com.asak.admin.dto.request.CreateMenuOptionItemRequest;
import com.asak.admin.dto.request.CreateMenuNutritionRequest;
import com.asak.admin.dto.request.CreateMenuRequest;
import com.asak.admin.dto.request.CreateMenuTagRequest;
import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.AdminCategoryResponse;
import com.asak.admin.dto.response.IngredientResponse;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;
import com.asak.admin.mapper.AdminMenuMapper;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.PageResult;
import com.asak.common.util.FileUtil;

@Service
public class AdminMenuService {

  private static final String GROUP_MENU_INGREDIENT_ROLE = "MENU_INGREDIENT_ROLE";
  private static final String GROUP_UNIT_TYPE = "UNIT_TYPE";

  private final AdminMenuMapper adminMenuMapper;

  public AdminMenuService(AdminMenuMapper adminMenuMapper) {
    this.adminMenuMapper = adminMenuMapper;
  }

  @Value("${app.file.menu-upload-dir}")
  private String menuUploadDir;

  public PageResult<MenuListResponse> getMenus(MenuListRequest request) {
    List<MenuListResponse> content = adminMenuMapper.getMenus(request);
    long totalElements = adminMenuMapper.countMenus(request);
    return new PageResult<>(content, request.getPage(), request.getSize(), totalElements);
  }

  public MenuDetailResponse getMenuDetail(Long menuId) {
    return adminMenuMapper.getMenuDetail(menuId);
  }

  public List<AdminCategoryResponse> getCategories() {
    return adminMenuMapper.getCategories();
  }

  public PageResult<IngredientResponse> getIngredients() {
    List<IngredientResponse> content = adminMenuMapper.getIngredients();
    int size = content.size();
    return new PageResult<>(content, 0, Math.max(size, 1), size);
  }

  public String saveMenuImage(MultipartFile imageFile) throws IOException {
    return FileUtil.saveMenuImage(imageFile, Paths.get(menuUploadDir));
  }

  @Transactional
  public MenuDetailResponse createMenu(CreateMenuRequest request) {
    Map<String, Object> map = new HashMap<>();
    map.put("categoryId", request.getCategoryId());
    map.put("name", request.getName());
    map.put("price", request.getPrice());
    map.put("imageUrl", request.getImageUrl());
    map.put("description", request.getDescription());

    int inserted = adminMenuMapper.insertMenu(map);
    Object generatedId = map.get("menuId");
    if (inserted <= 0 || generatedId == null) {
      throw new CustomException(ErrorCode.MENU_INSERT_FAILED);
    }

    Long menuId = ((Number) generatedId).longValue();
    insertChildren(menuId, request);

    MenuDetailResponse created = adminMenuMapper.getMenuDetail(menuId);
    if (created == null) {
      throw new CustomException(ErrorCode.MENU_INSERT_FAILED);
    }
    return created;
  }

  /**
   * 메뉴 수정. 본문은 항상 갱신하고, 자식 섹션은 null이 아니면 교체(delete+insert)한다.
   * (요청 DTO vs 응답 DTO equals 비교는 타입이 달라 의미가 없어 제거)
   */
  @Transactional
  public MenuDetailResponse updateMenu(Long menuId, CreateMenuRequest request) {
    requireActiveMenu(menuId);

    Map<String, Object> map = new HashMap<>();
    map.put("menuId", menuId);
    map.put("request", request);
    int updated = adminMenuMapper.updateMenu(map);
    if (updated <= 0) {
      throw new CustomException(ErrorCode.MENU_UPDATE_FAILED);
    }

    if (request.getIngredients() != null) {
      adminMenuMapper.deleteMenuIngredients(menuId);
      insertIngredients(menuId, request.getIngredients());
    }
    if (request.getOptionGroups() != null) {
      adminMenuMapper.deleteMenuOptOverrides(menuId);
      adminMenuMapper.deleteMenuOptionGroups(menuId);
      insertOptionGroups(menuId, request.getOptionGroups());
    }
    if (request.getNutrition() != null) {
      adminMenuMapper.deleteMenuNutrition(menuId);
      insertNutrition(menuId, request.getNutrition());
    }
    if (request.getTags() != null) {
      adminMenuMapper.deleteMenuTags(menuId);
      insertTags(menuId, request.getTags());
    }

    return adminMenuMapper.getMenuDetail(menuId);
  }

  /**
   * Soft delete: menu.deleted_at 만 설정. 주문 이력(order_item) FK 유지.
   * 자식(menu_ing 등)은 복구/감사용으로 남긴다.
   */
  @Transactional
  public void deleteMenu(Long menuId) {
    requireActiveMenu(menuId);
    int deleted = adminMenuMapper.softDeleteMenu(menuId);
    if (deleted <= 0) {
      throw new CustomException(ErrorCode.MENU_DELETE_FAILED);
    }
  }

  private void requireActiveMenu(Long menuId) {
    if (menuId == null || menuId <= 0 || getMenuDetail(menuId) == null) {
      throw new CustomException(ErrorCode.MENU_NOT_FOUND);
    }
  }

  private void insertChildren(Long menuId, CreateMenuRequest request) {
    insertIngredients(menuId, request.getIngredients());
    insertOptionGroups(menuId, request.getOptionGroups());
    insertNutrition(menuId, request.getNutrition());
    insertTags(menuId, request.getTags());
  }

  private void insertIngredients(Long menuId, List<CreateMenuIngredientRequest> ingredients) {
    if (ingredients == null || ingredients.isEmpty()) {
      return;
    }

    int sortNo = 1;
    for (CreateMenuIngredientRequest ingredient : ingredients) {
      if (ingredient.getIngredientId() == null) {
        throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
      }

      String roleCode = normalizeRoleCode(ingredient.getRole());
      Long roleId = resolveRoleId(roleCode);
      Long unitId = null;
      if (ingredient.getUnit() != null && !ingredient.getUnit().isBlank()) {
        unitId = adminMenuMapper.findCommonCodeId(GROUP_UNIT_TYPE, ingredient.getUnit().trim());
        if (unitId == null) {
          throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
        }
      }

      Map<String, Object> row = new HashMap<>();
      row.put("menuId", menuId);
      row.put("ingredientId", ingredient.getIngredientId());
      row.put("roleId", roleId);
      row.put("quantity", ingredient.getQuantity() != null ? ingredient.getQuantity() : 0d);
      row.put("unitId", unitId);
      row.put("isDefault", ingredient.getIsDefault() == null || ingredient.getIsDefault());
      // 클라이언트가 true를 보내더라도 핵심 재료(CORE)는 제외 불가로 저장한다.
      row.put("canRemove", !"CORE".equals(roleCode)
          && (ingredient.getCanRemove() == null || ingredient.getCanRemove()));
      row.put("sortNo", sortNo++);
      adminMenuMapper.insertMenuIngredient(row);
    }
  }

  private Long resolveRoleId(String role) {
    String normalized = normalizeRoleCode(role);
    Long roleId = adminMenuMapper.findCommonCodeId(GROUP_MENU_INGREDIENT_ROLE, normalized);
    if (roleId == null) {
      throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
    }
    return roleId;
  }

  private String normalizeRoleCode(String role) {
    if (role == null || role.isBlank()) {
      return "DEFAULT";
    }
    String upper = role.trim().toUpperCase(Locale.ROOT);
    if ("PLAIN".equals(upper) || "STANDARD".equals(upper)) {
      return "DEFAULT";
    }
    return upper;
  }

  private void insertOptionGroups(Long menuId, List<CreateMenuOptionGroupRequest> optionGroups) {
    if (optionGroups == null || optionGroups.isEmpty()) {
      return;
    }

    int sortNo = 1;
    for (CreateMenuOptionGroupRequest group : optionGroups) {
      if (group.getOptionGroupId() == null) {
        throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
      }
      Long policyId = adminMenuMapper.findOptPolicyId(group.getOptionGroupId());
      if (policyId == null) {
        throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
      }
      Map<String, Object> row = new HashMap<>();
      row.put("menuId", menuId);
      row.put("policyId", policyId);
      row.put("sortNo", sortNo++);
      row.put("required", Boolean.TRUE.equals(group.getIsRequired()) ? 1 : 0);
      adminMenuMapper.insertMenuOptPolicy(row);

      Long recommendedOptionItemId = resolveRecommendedOptionItemId(group);
      if (recommendedOptionItemId != null) {
        insertRecommendedOverrides(menuId, policyId, recommendedOptionItemId);
      }
    }
  }

  private Long resolveRecommendedOptionItemId(CreateMenuOptionGroupRequest group) {
    if (group.getRecommendedOptionItemId() != null) {
      return group.getRecommendedOptionItemId();
    }
    if (group.getItems() == null) {
      return null;
    }
    return group.getItems().stream()
        .filter(item -> Boolean.TRUE.equals(item.getIsRecommended()))
        .map(CreateMenuOptionItemRequest::getOptionItemId)
        .filter(id -> id != null)
        .findFirst()
        .orElse(null);
  }

  private void insertRecommendedOverrides(Long menuId, Long policyId, Long recommendedOptionItemId) {
    List<Long> optionItemIds = adminMenuMapper.findOptItemIdsByPolicyId(policyId);
    if (optionItemIds == null || optionItemIds.isEmpty()) {
      throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
    }
    if (!optionItemIds.contains(recommendedOptionItemId)) {
      throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
    }

    for (Long optionItemId : optionItemIds) {
      Map<String, Object> override = new HashMap<>();
      override.put("menuId", menuId);
      override.put("optionItemId", optionItemId);
      override.put("recommended", optionItemId.equals(recommendedOptionItemId) ? 1 : 0);
      adminMenuMapper.upsertMenuOptOverride(override);
    }
  }

  private void insertNutrition(Long menuId, CreateMenuNutritionRequest nutrition) {
    if (nutrition == null) {
      return;
    }
    if (nutrition.getKcal() == null
        && nutrition.getCarbG() == null
        && nutrition.getProteinG() == null
        && nutrition.getFatG() == null
        && nutrition.getSodiumMg() == null) {
      return;
    }

    Map<String, Object> row = new HashMap<>();
    row.put("menuId", menuId);
    row.put("kcal", nutrition.getKcal());
    row.put("carbG", nutrition.getCarbG());
    row.put("proteinG", nutrition.getProteinG());
    row.put("fatG", nutrition.getFatG());
    row.put("sodiumMg", nutrition.getSodiumMg());
    adminMenuMapper.insertMenuNutrition(row);
  }

  private void insertTags(Long menuId, List<CreateMenuTagRequest> tags) {
    if (tags == null || tags.isEmpty()) {
      return;
    }

    for (CreateMenuTagRequest tag : tags) {
      Long tagId = adminMenuMapper.findTagId(tag.getCode(), tag.getName());
      if (tagId == null) {
        throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
      }
      Map<String, Object> row = new HashMap<>();
      row.put("menuId", menuId);
      row.put("tagId", tagId);
      adminMenuMapper.insertMenuTag(row);
    }
  }
}
