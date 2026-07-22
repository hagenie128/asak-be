# ASAK 구현 작업대

> **문서를 처음부터 읽지 않는다.** 지금 만들 기능을 골라서 해당 블록만 본다.  
> 이 폴더는 Product Bible을 다시 쓴 문서가 아니라, 구현 중 필요한 정보를 빠르게 찾는 **작업용 인덱스**다.

## 지금 하는 일 선택

| 지금 만들거나 고칠 것 | 바로 열기 | 그 안에서 바로 확인하는 것 |
| --- | --- | --- |
| 키오스크 메뉴 목록·상세·옵션 | [Kiosk: 메뉴 목록](02-kiosk-implementation.md#scr-003--menu-list) | Figma, `GET` URL, 응답 필드, 품절/오류 |
| 키오스크 장바구니·주문 생성 | [Kiosk: 장바구니](02-kiosk-implementation.md#scr-005--cart) | request body, 주문 응답, 가격/품절 실패 |
| 키오스크 결제·완료 | [Kiosk: 결제](02-kiosk-implementation.md#scr-007--payment) | 결제 request, 성공/실패 응답, 재시도 |
| 키오스크 timeout·접근성 | [Kiosk: Timeout](02-kiosk-implementation.md#scr-013--timeout) | 경고/복귀/High Contrast |
| 관리자 실시간 주문·주문 관리 | [Admin: 실시간 주문](03-admin-implementation.md#scr-009--live-order-board) | 조회/상태 변경, TTS, 충돌 복구 |
| 관리자 품절·메뉴 | [Admin: 품절](03-admin-implementation.md#scr-011--sold-out-management) | 저장 필드, 품절 영향, 저장 실패 |
| 관리자 결제 수단·매출·대시보드 | [Admin: 대시보드](03-admin-implementation.md#scr-022--dashboard) | 기간 query, 집계 필드, Empty/Error |
| 영수증·멤버십·쿠폰 확장 | [Extension 화면](07-extension-implementation.md) | 출력/스캔 상태, 미확정 API와 보류 기준 |
| API 공통 처리·기존 mock 연결 | [API 공통 규칙](04-api-db-implementation.md) | envelope, error, adapter, Backend 진행 상태 |
| UI 컴포넌트/토큰 | [UI 컴포넌트](05-ui-component-guide.md) | 기존 컴포넌트 재사용, Figma 상태 |
| Figma 화면 상태를 하나씩 대조 | [상태 체크리스트](09-figma-state-checklist.md) | Default/Loading/Empty/Error/Saving/복구 |
| Figma 완료처럼 보이는데 코드/정책 갭 | [0718 프로젝트 갭](../design/figma-0718-project-gap.md) | UI 이식 vs 로직·API 연결 |
| 테스트·시연 직전 | [QA](06-qa-release-guide.md) | 화면 상태, P0 시나리오 |

## 작업할 때 보는 순서

1. 위 표에서 **기능 한 개**를 연다.
2. 해당 블록의 Figma 링크에서 Default와 Loading·Empty·Error·Disabled를 본다.
3. 같은 블록의 API 표에서 URL·request·응답 필드만 가져온다.
4. 실제 Kiosk/Admin/Backend 코드에서 기존 route·store·컴포넌트를 찾는다.
5. 블록 하단의 `완료 체크`만 통과시키고 다음 기능으로 넘어간다.

## 공통으로 딱 세 가지만 기억

- 실제 Backend business API는 아직 구현 전이다. 문서의 API는 **목표 계약**이며 mock과 실제 호출을 구분한다.
- 가격·품절·주문 상태 전이는 화면이 확정하지 않는다. 서버 응답을 최종값으로 사용한다.
- **Figma 정본:** `0718` (`yHhvn5RKjBd91U8BJUQz7F`) — `05-C`/`06-C`/`07-C` 캔버스. [figma-guide](../design/figma-guide.md)
- Screen Registry에는 Kiosk 9개·Admin 10개·Extension 2개가 있다. Figma에는 같은 화면의 세부 상태 프레임이 별도로 있으므로 Default만 구현하고 끝내지 않는다.

## 기준 화면 빠른 링크 (0718)

| 용도 | Figma |
| --- | --- |
| 파일 구조 확인 | [00-C Cover / File Map](https://www.figma.com/design/yHhvn5RKjBd91U8BJUQz7F/ASAK-%E2%80%94-Design-System---Product-UI-0718?node-id=174-8727) |
| 고객 키오스크 | [05-C Screens / Kiosk](https://www.figma.com/design/yHhvn5RKjBd91U8BJUQz7F/ASAK-%E2%80%94-Design-System---Product-UI-0718?node-id=134-7720) |
| 관리자 | [06-C Screens / Admin](https://www.figma.com/design/yHhvn5RKjBd91U8BJUQz7F/ASAK-%E2%80%94-Design-System---Product-UI-0718?node-id=134-10606) |
| 상태 QA | [07-C QA / Screen State Matrix](https://www.figma.com/design/yHhvn5RKjBd91U8BJUQz7F/ASAK-%E2%80%94-Design-System---Product-UI-0718?node-id=190-2) |

<details>
<summary>원본 정책이 서로 다를 때만 열기</summary>

우선순위는 `Decision/ADR → Screen Registry → Feature/API 문서 → 최신 Figma → 실제 코드`다. 실제 코드와 정책이 다르면 임의로 덮어쓰지 말고 차이를 기록한다.

- [Canonical Source](../product_bible/01_Foundation/docs/00-product-bible/CANONICAL_SOURCE.md)
- [Screen Registry](../product_bible/07_Screen_Bible/docs/07-screens/SCREEN_REGISTRY.md)
- [Product Bible Index](../governance/product-bible-index-2026-07-16.md)
</details>
