package com.asak.admin.dto.response.menus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuImageUploadResponse {
  private Long mediaAssetId;
  private String imageUrl;
}
