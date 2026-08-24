package com.asak.admin.dto.response;

import com.asak.common.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPaymentMethodResponse {
  private Long methodId; // 그대로 션 수정)은 범위
  private PaymentMethod methodCode;
  private String methodName;
  private String imageUrl;
  private String description;
  private Boolean active;
  private Integer sortNo;
}

// private Long methodId;
// private PaymentMethod methodCode;
// private String methodName;
// private Long imageAssetId;
// private String imageUrl;
// private String description;
// private boolean active;
// private int sortOrder;

// CREATE TABLE`pay_method_cfg`(`id`
// bigint NOT
// NULL AUTO_INCREMENT,`method_id`
// bigint NOT NULL,`name`

// varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
// `image_asset_id` bigint DEFAULT NULL,
// `description` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
// `active` tinyint(1) NOT NULL DEFAULT '1',
// `sort_no` int NOT NULL DEFAULT '0',

// PRIMARY KEY (`id`),
// UNIQUE KEY `method_id` (`method_id`),
// KEY `fk_pay_method_cfg_image_asset` (`image_asset_id`),
// CONSTRAINT `fk_pay_method_cfg_image_asset`

// FOREIGN KEY (`image_asset_id`) REFERENCES `media_asset` (`id`),
// CONSTRAINT `fk_payment_method_config_method`

// FOREIGN KEY (`method_id`) REFERENCES `common_code` (`id`)
// ) ENGINE=InnoDB AUTO_INCREMENT=10833 DEFAULT CHARSET=utf8mb4
// COLLATE=utf8mb4_unicode_ci;
