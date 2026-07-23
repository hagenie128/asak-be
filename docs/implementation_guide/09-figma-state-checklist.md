# 화면 상태 ↔ 백엔드 응답 체크리스트

> Figma는 화면 상태의 기준이고, 이 문서는 해당 상태를 만들기 위해 API가 무엇을 반환해야 하는지 정리한다.

| 화면 상태 | API 관점 | 확인 예 |
| --- | --- | --- |
| Default | 200/201과 화면용 data | 메뉴 카드, 주문 상세, 매출 KPI |
| Loading | 요청 중; 별도 응답 없음 | 프론트 timeout/cancel과 API 처리 시간 합의 |
| Empty | 200 + 빈 content/list | 카테고리 메뉴 없음, 활성 주문 없음 |
| Validation error | 400 + field 정보 | 수량, 필수 옵션, 날짜 형식 |
| Not found | 404 + target code | 존재하지 않는 menuId/orderId |
| Conflict | 409 + 재시도/복구 정보 | 품절, 가격 변경, 상태 전이, 중복 결제 |
| Server error | 500 + 안전한 code/message | DB/예상 밖 오류, 민감 정보 미노출 |
| Disabled | 결제수단/메뉴 상태 data | `isEnabled: false`, `isSoldOut: true` |

## 화면별 우선 확인

- SCR-003/004: 메뉴 목록·상세의 빈 목록, 품절, 메뉴 없음
- SCR-005/007/008/012: 옵션 검증, 가격/품절 충돌, 결제 중복/실패, 완료 data
- SCR-009/010: 활성 주문 없음, 상세 없음, 상태 전이 충돌
- SCR-011/016: 품절 변경 실패, 메뉴 폼 validation
- SCR-019~022: 기간 오류, 빈 매출, 취소/환불 반영 집계

Figma 화면에 표현된 문구는 Product Bible/프론트에서 매핑할 수 있다. 백엔드는 화면 문구가 아닌 안정적인 `code`, `status`, 필요한 `data`를 제공한다.
