# Extension 화면 작업 카드

> 현재 MVP 밖이지만 Screen Registry에 있는 화면이다. **구현하지 않더라도 API가 미확정인 상태를 알고 보류**한다.

## SCR-023 · Receipt Output

**Route:** `extension` · **목적:** 결제 완료 뒤 영수증 출력 여부를 선택하고 결과를 알려 준다.  
**상태:** `choice` → `printing` → `success` 또는 `error`

| 행동 | 필요한 값 | 처리 |
| --- | --- | --- |
| 출력 선택 | `printRequest` | 결제 완료 정보에서 출력 요청을 만든다. |
| 출력 진행 | `printResult` | printing 중 중복 출력 요청을 막는다. |
| 성공/실패 | 출력 성공 여부, 실패 원인 | 용지 없음/프린터 실패/네트워크 실패를 구분하고 재출력 행동을 준다. |

**목표 API:** `POST /api/kiosk/receipts` · DTO/응답 필드는 현재 별도 확정 필요.  
**완료 체크:** [ ] 출력 선택/진행/성공/실패가 있다. [ ] 중복 클릭이 중복 출력하지 않는다. [ ] 실패 뒤 재출력할 수 있다.

## SCR-024 · Membership / Coupon

**Route:** `extension` · **목적:** 회원 식별 뒤 적립/쿠폰 혜택을 결제 흐름에 적용한다.  
**상태:** `default` → `scanning` → `success` 또는 `error`

| 행동 | 필요한 값 | 처리 |
| --- | --- | --- |
| 회원/쿠폰 입력·스캔 | `scanResult` | scanning 중 중복 스캔을 막는다. |
| 혜택 확인 | `benefits` | 적용 가능한 혜택과 적용 결과를 분명히 구분한다. |
| 실패 | 식별/쿠폰 검증 실패 원인 | 주문·결제를 임의로 취소하지 않고 재입력/건너뛰기를 제공한다. |

**API:** `membership/coupon extension API`는 endpoint·중복 적립·쿠폰 검증 정책이 미확정이다. 구현 전에 계약을 먼저 확정한다.  
**완료 체크:** [ ] 기본/스캔/성공/실패가 있다. [ ] 중복 적립·중복 쿠폰을 막는다. [ ] 실패 뒤 결제 흐름을 잃지 않는다.

<details>
<summary>원본 Screen Bible</summary>

- [SCR-023 Receipt Output](../product_bible/07_Screen_Bible/docs/07-screens/SCR-023-RECEIPT-OUTPUT.md)
- [SCR-024 Membership / Coupon](../product_bible/07_Screen_Bible/docs/07-screens/SCR-024-MEMBERSHIP---COUPON.md)
</details>
