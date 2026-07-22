# ASAK 구현 고정 규칙

> 세부 요구사항의 정본은 Product Bible과 최신 Figma다. 이 문서는 구현 중 놓치기 쉬운 공통 기준만 모은다.

## 제품 기준

- ASAK는 샐러드 키오스크 주문과 매장 운영을 위한 서비스다. 사용자는 설명 없이 주문을 끝내고, 관리자는 짧은 시간 안에 매장 상태를 파악할 수 있어야 한다.
- 한 화면은 하나의 주된 결정을 돕는다.
- Default뿐 아니라 Loading, Empty, Error, Disabled, 완료·복구 상태도 제품의 일부다.
- 문제가 생겼을 때 사용자를 탓하지 않고, 현재 상태와 다음 행동을 명확히 보여 준다.
- MVP 범위를 우선한다. 문서에 미래 기능으로 표시된 것은 현재 작업에 임의로 포함하지 않는다.

## 기술과 책임 경계

| 영역 | 기준 |
| --- | --- |
| Kiosk / Admin | 별도 React JavaScript 앱으로 유지한다. |
| Frontend | React, React Router, Zustand, Axios, Vite 기준. TypeScript·Tailwind를 임의 도입하지 않는다. |
| Backend | Spring Boot 4.1.0, Java 25. Controller → Service → Repository → Entity/DB 책임을 지킨다. |
| 데이터 | DB는 snake_case, JSON은 camelCase를 사용한다. Entity를 API 응답으로 직접 내보내지 않는다. |
| 비즈니스 규칙 | 서버가 가격 재계산, 품절 영향, 주문 상태 전이를 최종 검증한다. |

## 변경할 때의 규칙

- 문서 때문에 현재 scaffold나 팀원 코드를 전면 이동·재작성하지 않는다.
- 페이지는 route와 상태 조합을 맡고, 재사용 UI는 component로, 기능 규칙은 feature/store/api 계층으로 분리한다.
- API URL 문자열·복잡한 계산을 page에 흩뿌리지 않는다.
- 응답·오류·상태값을 프론트에서 추측하지 않는다. 원본 API 계약이 Draft이면 백엔드 구현과 팀 결정으로 확정한 뒤 반영한다.
- 주문 번호와 내부 ID를 같은 값으로 취급하지 않는다.
- 금액은 표시 형식도 전체 화면에서 일관되어야 한다.

## 주문·결제에서 절대 놓치면 안 되는 것

- 클라이언트 장바구니 검증은 UX용이다. 서버 검증을 대체하지 않는다.
- 주문 생성 시 서버는 메뉴/옵션/품절/수량을 확인하고 가격을 다시 계산한다.
- 결제 중복, 중복 완료, 중복 TTS, 중복 매출 반영이 발생하지 않아야 한다.
- 결제 실패는 장바구니를 초기화하는 이유가 아니다.
- 처리 중 결제 화면의 뒤로가기와 timeout 처리에는 별도 정책이 필요하다.

## 정본 링크

- [Product Principles](../product_bible/01_Foundation/docs/00-product-bible/PRODUCT_PRINCIPLES.md)
- [Canonical Source](../product_bible/01_Foundation/docs/00-product-bible/CANONICAL_SOURCE.md)
- [Master Context](../product_bible/01_Foundation/docs/10-ai/MASTER_CONTEXT.md)
- [Frontend Architecture](../product_bible/06_Engineering_Bible/docs/02-frontend/FRONTEND_ARCHITECTURE.md)
- [Backend Architecture](../product_bible/06_Engineering_Bible/docs/03-backend/BACKEND_ARCHITECTURE.md)
