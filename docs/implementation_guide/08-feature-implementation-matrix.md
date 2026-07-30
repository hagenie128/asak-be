# 백엔드 기능 구현 매트릭스

> 기준일: 2026-07-23 · 표의 모든 API는 목표 계약이며 현재 실행 구현은 없다.

| 단계 | API | Method / 경로 | 현재 상태 | 선행 조건 |
| --- | --- | --- | --- | --- |
| 공통 | Health | `GET /api/health` | 미구현 | response/exception 최소 기반 |
| Kiosk 메뉴 | API-001 | `GET /api/kiosk/categories` | SPEC_ONLY | category Mapper/DTO |
| Kiosk 메뉴 | API-002 | `GET /api/kiosk/menuList` | SPEC_ONLY | menu 조회 SQL |
| Kiosk 메뉴 | API-003 | `GET /api/kiosk/menuDetail/{menuId}` | SPEC_ONLY | 옵션·재료 조인 |
| Kiosk 주문 | API-004 | `POST /api/kiosk/cart/validate` | SPEC_ONLY | 가격·품절 검증 |
| Kiosk 주문 | API-005 | `POST /api/kiosk/orders` | SPEC_ONLY | 주문 저장 transaction |
| Kiosk 결제 | API-014 | `GET /api/kiosk/payment-methods` | SPEC_ONLY | `pay_method_cfg` 조회 |
| Kiosk 결제 | API-006 | `POST /api/kiosk/payments` | SPEC_ONLY | 멱등성·상태 전이 |
| Admin 주문 | API-021 | `GET /api/admin/orders/live` | SPEC_ONLY | 주문 조회 |
| Admin 주문 | API-007/022 | `GET /api/admin/orders`, `/{orderId}` | SPEC_ONLY | pagination·상세 DTO |
| Admin 주문 | API-008 | `PATCH /api/admin/orders/{orderId}/{status}` | IMPLEMENTED | path `status` 상태 전이 |
| Admin 주문 | API-024 | `PATCH /api/admin/orders/{orderId}/cancel` | SPEC_ONLY | 환불·매출 반영 |
| Admin 운영 | API-009/010 | `PATCH` / `GET /api/admin/soldOut` | SPEC_ONLY | 품절 규칙 |
| Admin 메뉴 | API-011/023 | `GET /api/admin/menus`, `/{menuId}` | SPEC_ONLY | 목록/상세 조회 |
| Admin 메뉴 | API-012/013 | `POST` / `PATCH /api/admin/menus` | SPEC_ONLY | 메뉴 저장 transaction |
| Admin 결제 | API-015/016 | `GET` / `PATCH /api/admin/payment-methods` | SPEC_ONLY | 결제수단 설정 |
| Admin 통계 | API-020 | `GET /api/admin/dashboard` | SPEC_ONLY | 주문·매출 집계 |
| Admin 통계 | API-017/018/019 | `GET /api/admin/sales/daily|summary|monthly` | SPEC_ONLY | `vw_sales_*` 뷰 |

## 상태 표기 규칙

- `SPEC_ONLY`: Bruno 계약만 존재. Controller mapping과 실제 응답 미확인.
- `구현 중`: Controller부터 DB까지 한 세로 기능이 작업 중. 테스트 결과를 함께 기록.
- `검증됨`: 정상/오류 응답과 관련 회귀 테스트를 확인. 커밋 여부와는 별개.

다음 구현자는 위 표의 최상단 미구현 항목 하나만 골라 세로로 완료한 뒤 상태를 갱신한다.
