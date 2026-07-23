# ASAK Bruno API Contract Collection

> Status: SPEC_ONLY (2026-07-23)

이 컬렉션은 현재 Product Bible의 API 계약을 Bruno에서 검토하기 위한 요청 모음입니다.

## 사용 방법

1. Bruno에서 이 `api` 폴더를 Collection으로 연다.
2. `Local` 환경을 선택하고 `baseUrl`을 실행 중인 백엔드 주소로 맞춘다.
3. 구현된 Controller와 매핑된 요청만 실행한다.

`Local` 환경에는 검토용 ID가 들어 있다. 실제 테스트 데이터의 메뉴·주문·결제수단 ID와
다르면 해당 환경 변수만 바꾼다. 결제수단 기본값 `methodId: 10828`은 `CARD`이며 화면 이름은
`카드·삼성페이`다.

## 주의

- 현재 `ASAK-back`의 Controller에는 HTTP mapping annotation이 아직 없으므로, 이 요청들의 2xx 응답은 아직 기대하지 않는다.
- 성공 응답 자동 테스트는 구현 전 오해를 막기 위해 넣지 않았다.
- API 구현 후 해당 요청에 성공/오류 응답 테스트를 추가한다.
- `KAKAO_PAY`, `NAVER_PAY`는 목록에는 보이지만 현재 `isEnabled: false`다. Bruno의 결제
  승인 요청은 기본적으로 활성 수단 `CARD`를 사용한다.

## 기준 문서

- `ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md`
- `ASAK/docs/product_bible/03_Menu_Inventory_SoldOut/docs/09-features/menu/MENU_API_CONTRACT.md`
- `ASAK/docs/product_bible/02_Order_Cart_Payment/docs/09-features/*/*_API_*.md`
- `ASAK/docs/product_bible/04_Dashboard_Sales_Kitchen_TTS/docs/09-features/*/*_ARCHITECTURE.md`

모든 응답 계약은 `{ success, status, code, message, data }` 형식을 따른다.
