# Admin·Kiosk 연동 감사: 하드코딩과 API 계약

> 확인일: 2026-07-23
> 범위: `ASAK-Admin`, `ASAK-Kiosk`의 현재 소스·mock. 이 문서는 소스 변경 지시가 아니라 백엔드 API 연결 전 확인표다.

## 결론

한글 버튼·안내 문구, 날짜/금액 표시, Figma 전용 Empty/Error 컴포넌트의 기본 문구는 프론트 책임이다. 백엔드는 이를 그대로 내려주기보다 일관된 `status`, `code`, `data`를 반환한다.

반면 상태 코드, API 경로, 서버 확정 금액처럼 **비즈니스 의미를 가진 하드코딩**은 계약과 맞춰야 한다. 아래 항목은 API 구현/연동 전에 어댑터 또는 팀 합의가 필요하다.

## 확인된 계약 차이

| 구분 | 프론트 현재 값 | 백엔드 정본 | 처리 방향 |
| --- | --- | --- | --- |
| 주문 취소 상태 | Admin/Kiosk mock: `CANCELLED` | `CANCELED` | 2026-07-16 정본 계약이 adapter 정규화를 명시. 백엔드는 `CANCELED` 반환 |
| 결제 완료 상태 | Admin mock: `PAID` | `APPROVED` | Admin adapter에서 `APPROVED → PAID` 표시 매핑 또는 화면 코드 통일 |
| 결제수단 | Admin label에 `CASH`, mock 결제수단에는 `zero` | `CARD`, `KAKAO_PAY`, `NAVER_PAY` | `CASH`/`zero` 지원 여부와 methodId 타입을 별도 합의. 현재 백엔드는 임의 추가하지 않음 |
| Admin 품절 경로 | `/api/admin/sold-out-items` hint | `GET/PATCH /api/admin/soldOut` | API client 상수를 정본 경로로 교체할 때 연결 |
| 주문 금액 필드 | Kiosk/Admin mock/store: `totalPrice` | `totalAmount` | 정본 계약이 adapter 변환을 명시. 주문 요청의 금액은 서버 정본이 아님 |
| 완료 대기 수 | Kiosk mock: `waitingCount` | `waitingOrderCount` | Kiosk `orderAdapter`에서 변환 |
| 결제 승인 값 | Kiosk mock: `amount`, `paidAt` | `approvedAmount`, `approvedAt` | payment adapter에서 변환 |

## 이미 준비된 좋은 경계

- Admin `src/api/client.js`는 `{ success, status, code, message, data }` envelope를 한 곳에서 풀도록 되어 있다. 백엔드는 이 구조를 유지한다.
- Kiosk `src/adapters/orderAdapter.js`와 type 주석은 `totalPrice → totalAmount`, `waitingCount → waitingOrderCount` 변환 위치를 명시한다. 단, 현재 adapter는 payload를 그대로 반환하는 상태다.
- Admin mock README는 Live 카드의 `menus[]`/`tone`과 주문 목록·상세의 `items[]`/`optionItems[]`를 구분한다. 실제 API도 화면 전용 Live DTO와 관리용 목록/상세 DTO의 목적을 분리한다.
- Admin의 `orderLabels.js`는 코드값과 한국어 라벨을 분리해 두었다. 백엔드는 한국어 라벨 대신 코드값을 내려도 된다.

## 화면 문구와 서버 코드의 경계

| 항목 | 소유자 | 예 |
| --- | --- | --- |
| 버튼/기본 안내 문구 | Frontend/Figma | `결제하기`, `데이터가 없습니다`, `다시 시도` |
| 표시 포맷 | Frontend | 원화 표기, `오늘/이번 주/이번 달`, 날짜 문자열 |
| 실패 분기 기준 | Backend | HTTP 400/404/409/500, `MENU_SOLD_OUT`, `ORDER_PRICE_CHANGED` |
| 화면별 사용자 문구 매핑 | Frontend | code를 toast/empty/error 문구로 변환 |
| 최종 가격·품절·상태 | Backend | 서버 계산 `totalAmount`, 실제 `isSoldOut`, 주문/결제 상태 |

Kiosk `EmptyState`, `ErrorMessage`, 수량 제한 toast 문구는 현재 화면용 하드코딩이다. 이는 백엔드 메시지로 복제하지 않는다. 다만 수량 제한 `메뉴당 9개`, `장바구니 전체 30개`는 현재 프론트 로컬 규칙만 확인됐으므로, 주문 API에서 동일하게 강제할지는 Product Bible/팀 정책으로 확정한 뒤 Service 검증과 오류 code를 추가한다. 주문 생성 성공 응답은 주문 아키텍처의 최소 완료 data(`orderId`, `orderNo`, `RECEIVED`, `APPROVED`, `totalAmount`, `waitingOrderCount`)를 충족해야 한다.

## 연동 전 체크리스트

- [ ] Admin의 `CANCELLED`, `PAID`, `CASH` 처리 방식을 팀이 확정했다.
- [ ] Admin API path hint가 canonical 경로와 일치한다.
- [ ] Kiosk adapter가 mock 표시 필드와 서버 필드를 한 곳에서 변환한다.
- [ ] `POST /api/kiosk/orders`는 `totalPrice`/`amount`를 요청 정본으로 받지 않는다.
- [ ] Kiosk `apiError.js`에 code별 화면 동작을 연결한다.
- [ ] 서버 오류 message에 UI 문구·DB 상세·비밀값을 넣지 않는다.

## 확인 파일

- `ASAK-Admin/src/api/client.js`, `constants/orderLabels.js`, `constants/api.js`, `mocks/adminMockRepository.js`
- `ASAK-Kiosk/src/adapters/orderAdapter.js`, `constants/status.js`, `types/order.js`, `types/payment.js`, `utils/quantityLimits.js`, `pages/kiosk/PaymentPage.jsx`
- [정본 API 정리](../../../ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md)
- [정본 계약 결정](../../../ASAK/docs/governance/canonical-contract-decisions-2026-07-16.md)
- [주문 아키텍처](../../../ASAK/docs/product_bible/02_Order_Cart_Payment/docs/09-features/order/ORDER_ARCHITECTURE.md)
- [Admin mock 필드 사전](../../../ASAK-Admin/public/mocks/README.md)
- [ASAK-back 구현 계획](../../IMPLEMENTATION_PLAN.md)
