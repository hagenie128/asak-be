# ASAK Kiosk QA 실행 보고 (2026-09-02)

> Status: **CURRENT**  
> 범위: **API E2E** (브라우저 클릭·실기기 터치 없음) · **애플리케이션 코드 수정 없음**  
> 스크립트: [`scripts/qa-kiosk-api-2026-09-02.ps1`](../../scripts/qa-kiosk-api-2026-09-02.ps1)  
> JSON: [`qa-kiosk-api-results-2026-09-02.json`](qa-kiosk-api-results-2026-09-02.json)  
> 연계: [Admin QA](qa-execution-report-2026-09-02.md) · [TC 실행표](demo-tc-execution-sheet-2026-09-02.md)

## 결론

| 구분 | 결과 |
| --- | --- |
| Kiosk API E2E | **16/18 PASS** (스크립트) + K-004 수동 **PASS** (HTTP 404) |
| Kiosk build | **PASS** (`npm install` 후 `npm run build`) |
| Kiosk dev + proxy | **PASS** (`5175` → `/api/kiosk/menuList` 200) |
| Admin→Kiosk 결제수단 | **FAIL** (기존 Admin QA와 동일) |

**시연 가능:** 키오스크 **주문→결제→완료 API 흐름**은 실DB에서 동작 확인됨.  
**주의:** Admin 결제수단 OFF가 Kiosk에 반영되지 않음. 재료(ing125) 품절은 Kiosk 메뉴에 영향 없음(메뉴·옵션 단위 품절은 OK).

---

## P0 TC (Kiosk)

| TC | 결과 | 근거 |
| --- | --- | --- |
| **TC-001** orderType EAT_IN/TAKE_OUT | **PASS** | 주문 생성 + Admin 상세 `orderType=TAKE_OUT` 확인 |
| **TC-002** 주문→결제→완료 | **PASS** | `orders` → `payments` CARD APPROVED · `orderNo`·`approvedAmount` 일치 |
| **TC-003** 품절 비활성화 | **PASS** | MENU soldOut → `menuList.isSoldOut` + cart `MENU_SOLD_OUT` · OPTION_ITEM soldOut → `menuDetail` 반영 |
| **TC-012** 결제수단 Kiosk | **PASS** △ | 4종 조회 OK · **Admin OFF 반영 FAIL** |
| 결제 idempotency | **PASS** | 동일 key 재요청 → 동일 `paymentId` |
| 중복 결제 차단 | **PASS** | 이미 승인 주문 → `ORDER_STATUS_CONFLICT` 409 |
| 필수 옵션 누락 | **PASS** | menu 768 · `INVALID_OPTION_SELECTION` 400 |
| 빈 장바구니 | **PASS** | `CART_EMPTY` 400 |

---

## API 상세

| ID | 항목 | 결과 |
| --- | --- | --- |
| K-001 | `/categories` 8건 | PASS |
| K-002 | `/menuList` 72건 | PASS |
| K-003 | `/menuDetail/{id}` | PASS |
| K-004 | 존재하지 않는 메뉴 | PASS (HTTP 404, 수동 확인) |
| K-005 | `/cart/validate` 단순 메뉴 | PASS |
| K-006 | 빈 cart | PASS |
| K-007 | 필수 옵션 누락 | PASS |
| K-010 | `/payment-methods` 4종 | PASS |
| K-011 | `/orders/{id}/receipt-print` | PASS (200) |
| TC-003a | MENU 품절 Kiosk 반영 | PASS |
| TC-012-kiosk | Admin CARD OFF → Kiosk 숨김 | **FAIL** |

---

## Admin 연동 재확인

| 항목 | 결과 |
| --- | --- |
| 메뉴(MENU) 품절 → Kiosk | **PASS** |
| 옵션(OPTION_ITEM) 품절 → menuDetail | **PASS** |
| 재료(INGREDIENT) ing125 → menuList | **FAIL** △ (`affectedMenuCount=0`, 변화 없음) |
| 결제수단 Admin PATCH → Kiosk 목록 | **FAIL** |

---

## 시연용 참고 (QA 중 생성된 주문)

| orderNo | orderType | 금액 | 비고 |
| --- | --- | --- | --- |
| ASAK2609020001~ | EAT_IN/TAKE_OUT 등 | 4,500~11,400 | 실DB 테스트 주문 · 시연 전 정리 여부 팀 판단 |

---

## 재실행

```powershell
# Backend 8080 필수
powershell -File c:\ASAK-workspace\ASAK\scripts\qa-kiosk-api-2026-09-02.ps1
```

품절·결제수단 테스트는 스크립트/수동 검증 후 **원복** 포함.
