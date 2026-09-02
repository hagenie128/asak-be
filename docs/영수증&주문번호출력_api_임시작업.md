# 영수증 & 주문번호 출력 API 임시 작업 문서

> 작성일: 2026-09-01  
> 상태: 현재 백엔드 구현 기준 임시 명세  
> 주의: RTOS 실제 프린터 handler와 영수증 payload 계약이 확정되기 전까지 변경될 수 있다.

## 1. 문서 목적

키오스크의 `영수증 출력` 버튼과 `주문번호만 출력` 버튼을 눌렀을 때 프론트엔드, Spring Boot 백엔드, RTOS가 어떤 데이터를 주고받는지 정리한다.

이 문서에는 다음 내용을 포함한다.

- 영수증 출력 API의 Request와 Response
- 주문번호만 출력 API의 Request와 Response
- `requestId`와 `eventId`의 차이
- 장치 이벤트의 `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED` 응답
- RTOS polling 및 완료 보고 API
- 프론트엔드, 백엔드, RTOS의 역할
- 현재 키오스크에 적용된 부분과 아직 남아 있는 부분

## 2. 전체 구조

```text
키오스크 프론트엔드
  → 출력 요청 POST
Spring Boot
  → 출력 이벤트를 PENDING으로 등록
  → 프론트에 eventId 반환
RTOS
  → pending API를 주기적으로 polling
  → 이벤트를 가져가서 프린터 출력
  → COMPLETED 또는 FAILED 결과 보고
Spring Boot
  → 최종 상태 저장
키오스크 프론트엔드
  → eventId로 최종 상태 조회
```

### 구성요소별 역할

| 구성요소 | 담당 역할 | 직접 생성하는 주요 값 |
| --- | --- | --- |
| 키오스크 프론트엔드 | 버튼 클릭 처리, 출력 요청, 상태 조회 | `requestId` |
| Spring Boot Controller | URL과 Request Body 수신, Service 호출, 공통 응답 반환 | `requestSource=KIOSK` |
| Spring Boot Service | 주문 데이터 조회, `eventType`과 `payload` 구성 | `PRINT_WAITING_NUMBER`, 주문번호 payload |
| DeviceEventService | 출력 작업 등록과 상태 관리 | `eventId`, `PENDING`, `requestedAt` |
| MyBatis/MySQL | 주문에 저장된 고정 대기번호 조회 | `orders.waiting_order_no` |
| RTOS | 이벤트 polling, 프린터 출력, 처리 결과 보고 | `COMPLETED/FAILED`, `result` |

## 3. 공통 식별자와 필드

| 필드 | 생성 주체 | 예시 | 의미 |
| --- | --- | --- | --- |
| `orderId` | 주문 생성 백엔드 | `364` | DB `orders.id`의 PK이며 URL path로 전달 |
| `waitingOrderNo` | 결제 승인 백엔드 | `42` | 결제 완료 시 저장된 고객용 고정 대기번호 |
| `requestId` | 프론트엔드 | UUID | 한 번의 출력 요청을 식별하기 위한 클라이언트 값 |
| `eventId` | 백엔드 | `1` | 서버에 등록된 출력 작업 식별자 |
| `eventType` | 현재 API 또는 백엔드 | `PRINT_RECEIPT` | RTOS가 어떤 출력을 할지 구분 |
| `payload` | 현재 API 또는 백엔드 | `"42"` | RTOS가 실제 출력할 데이터 |
| `requestSource` | 백엔드 | `KIOSK` | 키오스크 요청과 관리자 요청 구분 |
| `status` | 백엔드/RTOS | `PENDING` | 출력 작업 처리 상태 |
| `result` | RTOS | `printed` | 출력 성공 또는 실패 결과 설명 |

### requestId와 eventId 비교

| 구분 | requestId | eventId |
| --- | --- | --- |
| 생성하는 곳 | 프론트엔드 | Spring Boot |
| 생성 시점 | 출력 버튼 클릭 시 | 출력 이벤트 등록 시 |
| 형식 | UUID 문자열 | 현재 메모리 증가 숫자 |
| 프론트가 POST로 전송하는가 | 예 | 아니요 |
| POST 응답으로 받는가 | 현재 응답 DTO에는 없음 | 예 |
| 용도 | 중복 요청 식별 목적 | 출력 상태 조회 및 RTOS 완료 보고 |
| 현재 중복 방지 동작 | 아직 없음 | 이벤트별 고유 번호로 사용 |

프론트의 UUID 생성 예시:

```js
const requestId = crypto.randomUUID();
```

현재 백엔드는 `requestId`가 UUID 형식인지 검사하지 않고 `@NotBlank`로 빈 문자열 여부만 검사한다. 또한 같은 `requestId`를 다시 보냈을 때 중복 이벤트 생성을 막는 로직은 아직 없다.

## 4. 프론트엔드 API endpoint

```js
export const API_BASE_PATH = "/api/kiosk";

export const API_ENDPOINTS = Object.freeze({
  orderReceipt: (orderId) =>
    `${API_BASE_PATH}/orders/${orderId}/receipt-print`,

  orderNoReceipt: (orderId) =>
    `${API_BASE_PATH}/orders/${orderId}/waiting-number-print`,

  deviceEvent: (eventId) =>
    `${API_BASE_PATH}/orders/device-events/${eventId}`,
});
```

| 프론트 기능 | Method | endpoint |
| --- | --- | --- |
| 영수증 출력 요청 | POST | `/api/kiosk/orders/{orderId}/receipt-print` |
| 주문번호만 출력 요청 | POST | `/api/kiosk/orders/{orderId}/waiting-number-print` |
| 출력 상태 조회 | GET | `/api/kiosk/orders/device-events/{eventId}` |

## 5. 영수증 출력 API

### 5.1 기본 명세

| 항목 | 내용 |
| --- | --- |
| 기능 | 영수증 출력 이벤트 등록 |
| Method | `POST` |
| URL | `/api/kiosk/orders/{orderId}/receipt-print` |
| Content-Type | `application/json` |
| Path Variable | `orderId: long` |
| Request DTO | `CreateDeviceEventRequest` |
| 성공 code | `KIOSK_RECEIPT_PRINT_REQUESTED` |
| 최초 상태 | `PENDING` |

### 5.2 현재 Request 명세

현재 영수증 endpoint는 프론트에서 다음 세 값을 모두 받는다.

| 필드 | 타입 | 필수 | 예시 | 설명 |
| --- | --- | --- | --- | --- |
| `eventType` | String | 필수 | `PRINT_RECEIPT` | RTOS 출력 종류 |
| `payload` | String | 필수 | 영수증 문자열 | RTOS가 출력할 영수증 내용 |
| `requestId` | String | 필수 | UUID | 프론트 출력 요청 식별자 |

```http
POST /api/kiosk/orders/364/receipt-print
Content-Type: application/json
```

```json
{
  "eventType": "PRINT_RECEIPT",
  "payload": "영수증 출력 데이터",
  "requestId": "receipt-364-550e8400-e29b-41d4-a716-446655440000"
}
```

Axios 예시:

```js
const response = await axios.post(
  API_ENDPOINTS.orderReceipt(orderId),
  {
    eventType: "PRINT_RECEIPT",
    payload: receiptPayload,
    requestId: crypto.randomUUID(),
  }
);
```

### 5.3 출력 작업 등록 성공 Response

```json
{
  "success": true,
  "status": 200,
  "code": "KIOSK_RECEIPT_PRINT_REQUESTED",
  "message": "영수증 출력 요청을 등록했습니다.",
  "data": {
    "eventId": 1,
    "orderId": 364,
    "eventType": "PRINT_RECEIPT",
    "payload": "영수증 출력 데이터",
    "requestSource": "KIOSK",
    "status": "PENDING",
    "result": null,
    "requestedAt": "2026-09-01T00:30:00Z",
    "completedAt": null
  }
}
```

이 응답은 `실제 영수증 출력 성공`이 아니라 `백엔드에 출력 작업 등록 성공`을 의미한다.

### 5.4 현재 적용 상태

| 항목 | 상태 | 설명 |
| --- | --- | --- |
| 영수증 POST endpoint | 적용 | `UserReceiptController`에 존재 |
| `PRINT_RECEIPT` 이벤트 등록 | 적용 | 프론트가 보낸 eventType과 payload로 PENDING 생성 |
| eventId 반환 | 적용 | DeviceEventService가 생성 |
| 백엔드의 주문 존재 검증 | 미적용 | 현재 Controller가 바로 DeviceEventService 호출 |
| 백엔드의 영수증 데이터 DB 조회 | 미적용 | 프론트가 payload를 보내는 구조 |
| 영수증 payload 정식 계약 | 미확정 | RTOS와 필드/문자열 형식 합의 필요 |
| RTOS 영수증 handler 실제 실행 | 확인하지 못함 | 현재 백엔드 저장소에는 RTOS 구현 코드가 없음 |
| 실제 프린터 출력 테스트 | 실행하지 않음 | RTOS와 프린터 실행 필요 |

## 6. 주문번호만 출력 API

### 6.1 기본 명세

| 항목 | 내용 |
| --- | --- |
| 기능 | 결제 완료 주문의 고정 대기번호 출력 이벤트 등록 |
| Method | `POST` |
| URL | `/api/kiosk/orders/{orderId}/waiting-number-print` |
| Content-Type | `application/json` |
| Path Variable | `orderId: long` |
| Request DTO | `CreatePrintRequest` |
| 성공 code | `KIOSK_WAITING_NUMBER_PRINT_REQUESTED` |
| RTOS eventType | `PRINT_WAITING_NUMBER` |
| 최초 상태 | `PENDING` |

### 6.2 Request 명세

프론트는 주문번호 자체를 보내지 않고 `requestId`만 보낸다. `orderId`는 URL path로 전달한다.

| 위치 | 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- | --- |
| Path | `orderId` | long | 필수 | 출력할 주문의 DB PK |
| Body | `requestId` | String | 필수 | 프론트에서 생성한 UUID |

```http
POST /api/kiosk/orders/364/waiting-number-print
Content-Type: application/json
```

```json
{
  "requestId": "waiting-number-364-550e8400-e29b-41d4-a716-446655440000"
}
```

Axios 예시:

```js
const response = await axios.post(
  API_ENDPOINTS.orderNoReceipt(orderId),
  {
    requestId: crypto.randomUUID(),
  }
);

const eventId = response.data.data.eventId;
```

### 6.3 백엔드 내부 처리

```text
orderId와 requestId 수신
→ UserReceiptService 호출
→ orders.id로 waiting_order_no 조회
→ 조회값이 42이면 payload를 "42"로 변환
→ eventType을 PRINT_WAITING_NUMBER로 고정
→ requestSource를 KIOSK로 지정
→ eventId 생성
→ PENDING 이벤트 저장
```

백엔드가 만드는 내부 명령:

```json
{
  "eventType": "PRINT_WAITING_NUMBER",
  "payload": "42",
  "requestId": "waiting-number-364-550e8400-e29b-41d4-a716-446655440000"
}
```

### 6.4 출력 작업 등록 성공 Response

```json
{
  "success": true,
  "status": 200,
  "code": "KIOSK_WAITING_NUMBER_PRINT_REQUESTED",
  "message": "주문 번호 출력 요청을 등록했습니다.",
  "data": {
    "eventId": 2,
    "orderId": 364,
    "eventType": "PRINT_WAITING_NUMBER",
    "payload": "42",
    "requestSource": "KIOSK",
    "status": "PENDING",
    "result": null,
    "requestedAt": "2026-09-01T00:31:00Z",
    "completedAt": null
  }
}
```

### 6.5 주문번호 조회 실패 Response

현재 `waiting_order_no` 조회 결과가 null이면 `ORDER_NOT_FOUND`를 반환한다.

```json
{
  "success": false,
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "data": null
}
```

현재는 다음 두 경우가 동일한 오류로 처리될 수 있다.

| 실제 상황 | 현재 결과 |
| --- | --- |
| orderId에 해당하는 주문이 없음 | `ORDER_NOT_FOUND` |
| 주문은 있지만 결제 전이라 waiting_order_no가 NULL | `ORDER_NOT_FOUND` |

추후 `WAITING_ORDER_NUMBER_NOT_ASSIGNED`와 같은 별도 오류를 두는 것을 검토한다.

### 6.6 현재 적용 상태

| 항목 | 상태 | 설명 |
| --- | --- | --- |
| 주문번호 출력 POST endpoint | 적용 | `UserReceiptController`에 추가됨 |
| 프론트 requestId 수신 | 적용 | `CreatePrintRequest` 사용 |
| orderId 기준 DB 조회 | 적용 | `UserPayMapper`와 XML에 조회 추가 |
| `PRINT_WAITING_NUMBER` 고정 | 적용 | `UserReceiptService`에서 생성 |
| payload에 waitingOrderNo 설정 | 적용 | DB 번호를 String으로 변환 |
| PENDING 이벤트 등록 및 eventId 반환 | 적용 | DeviceEventService 사용 |
| RTOS `PRINT_WAITING_NUMBER` handler | 추가 필요/확인 필요 | handler가 없으면 실제 출력 안 됨 |
| 실제 프린터 출력 테스트 | 실행하지 않음 | RTOS 실행 후 통합 테스트 필요 |

## 7. Device event 상태 조회 API

### 7.1 기본 명세

| 항목 | 내용 |
| --- | --- |
| 기능 | 출력 작업의 현재 상태 조회 |
| Method | `GET` |
| URL | `/api/kiosk/orders/device-events/{eventId}` |
| Request Body | 없음 |
| Path Variable | `eventId: long` |
| 성공 code | `OK` |

```js
const response = await axios.get(
  API_ENDPOINTS.deviceEvent(eventId)
);
```

### 7.2 Response 필드

| 필드 | 타입 | null 가능 | 설명 |
| --- | --- | --- | --- |
| `eventId` | long | 아니요 | 출력 작업 번호 |
| `orderId` | long | 아니요 | 출력 대상 주문 PK |
| `eventType` | String | 아니요 | 출력 종류 |
| `payload` | String | 아니요 | 실제 출력 데이터 |
| `requestSource` | String | 아니요 | `KIOSK` 또는 `ADMIN` |
| `status` | String | 아니요 | 현재 처리 상태 |
| `result` | String | 예 | RTOS 처리 결과 |
| `requestedAt` | Instant | 아니요 | 요청 등록 시각 |
| `completedAt` | Instant | 예 | 완료 또는 실패 시각 |

현재 응답에는 `requestId`가 포함되지 않는다.

### 7.3 상태값 비교

| status | 의미 | RTOS 상태 | 프론트 처리 |
| --- | --- | --- | --- |
| `PENDING` | 작업 등록 후 아직 가져가지 않음 | RTOS 미실행 또는 다음 polling 대기 | 출력 대기 표시 |
| `PROCESSING` | RTOS가 작업을 claim함 | 출력 처리 중 | 중복 버튼 클릭 방지 |
| `COMPLETED` | RTOS가 성공 보고 | 출력 성공 | 출력 완료 안내 |
| `FAILED` | RTOS가 실패 보고 | 출력 실패 | 실패 사유와 재시도 안내 |

### 7.4 PENDING Response

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "요청에 성공했습니다.",
  "data": {
    "eventId": 2,
    "orderId": 364,
    "eventType": "PRINT_WAITING_NUMBER",
    "payload": "42",
    "requestSource": "KIOSK",
    "status": "PENDING",
    "result": null,
    "requestedAt": "2026-09-01T00:31:00Z",
    "completedAt": null
  }
}
```

### 7.5 출력 성공 Response

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "요청에 성공했습니다.",
  "data": {
    "eventId": 2,
    "orderId": 364,
    "eventType": "PRINT_WAITING_NUMBER",
    "payload": "42",
    "requestSource": "KIOSK",
    "status": "COMPLETED",
    "result": "printed",
    "requestedAt": "2026-09-01T00:31:00Z",
    "completedAt": "2026-09-01T00:31:03Z"
  }
}
```

### 7.6 출력 실패 Response

HTTP 상태는 상태 조회에 성공했기 때문에 200이고, 실제 장치 작업 실패 여부는 `data.status`로 판단한다.

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "요청에 성공했습니다.",
  "data": {
    "eventId": 2,
    "orderId": 364,
    "eventType": "PRINT_WAITING_NUMBER",
    "payload": "42",
    "requestSource": "KIOSK",
    "status": "FAILED",
    "result": "PRINTER_OUT_OF_PAPER",
    "requestedAt": "2026-09-01T00:31:00Z",
    "completedAt": "2026-09-01T00:31:03Z"
  }
}
```

프론트 판단 예시:

```js
const event = response.data.data;

switch (event.status) {
  case "PENDING":
  case "PROCESSING":
    // 아직 실제 출력 완료가 아님
    break;

  case "COMPLETED":
    // 실제 출력 완료 안내
    break;

  case "FAILED":
    // event.result를 이용해 실패 안내
    break;
}
```

## 8. RTOS API

### 8.1 pending 작업 polling

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| URL | `/api/rtos/device-events/pending` |
| 호출 주체 | RTOS |
| 동작 | 가장 오래된 PENDING 이벤트를 PROCESSING으로 바꾸고 반환 |

작업이 있는 경우:

```json
{
  "success": true,
  "status": 200,
  "code": "RTOS_DEVICE_EVENT_CLAIMED",
  "message": "처리할 장치 이벤트입니다.",
  "data": {
    "eventId": 2,
    "orderId": 364,
    "eventType": "PRINT_WAITING_NUMBER",
    "payload": "42",
    "requestSource": "KIOSK",
    "status": "PROCESSING",
    "result": null,
    "requestedAt": "2026-09-01T00:31:00Z",
    "completedAt": null
  }
}
```

작업이 없는 경우:

```json
{
  "success": true,
  "status": 200,
  "code": "RTOS_DEVICE_EVENT_EMPTY",
  "message": "처리할 장치 이벤트가 없습니다.",
  "data": null
}
```

### 8.2 RTOS 완료 결과 보고

| 항목 | 내용 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/rtos/device-events/{eventId}/finish` |
| 호출 주체 | RTOS |
| Request Body | `status`, `result` |
| 허용 상태 | `COMPLETED`, `FAILED` |

성공 보고 Request:

```json
{
  "status": "COMPLETED",
  "result": "printed"
}
```

실패 보고 Request:

```json
{
  "status": "FAILED",
  "result": "PRINTER_OUT_OF_PAPER"
}
```

완료 보고 Response:

```json
{
  "success": true,
  "status": 200,
  "code": "RTOS_DEVICE_EVENT_FINISHED",
  "message": "RTOS 처리 결과를 반영했습니다.",
  "data": {
    "eventId": 2,
    "orderId": 364,
    "eventType": "PRINT_WAITING_NUMBER",
    "payload": "42",
    "requestSource": "KIOSK",
    "status": "COMPLETED",
    "result": "printed",
    "requestedAt": "2026-09-01T00:31:00Z",
    "completedAt": "2026-09-01T00:31:03Z"
  }
}
```

RTOS는 `eventType`을 기준으로 handler를 구분해야 한다.

```c
if (strcmp(eventType, "PRINT_RECEIPT") == 0) {
    print_receipt(payload);
} else if (strcmp(eventType, "PRINT_WAITING_NUMBER") == 0) {
    print_waiting_number(payload);
}
```

## 9. 버튼 클릭 후 실제 동작 판단

| 브라우저/상태 조회 결과 | 판단 | 확인할 곳 |
| --- | --- | --- |
| POST 요청 실패 | 출력 작업이 등록되지 않음 | 프론트 URL, body, 백엔드 오류 |
| POST 200 + PENDING | 프론트와 백엔드 연결 성공 | RTOS 실행 및 polling 확인 |
| PENDING에서 계속 멈춤 | RTOS가 작업을 가져가지 않음 | RTOS 미실행 또는 네트워크 문제 |
| PROCESSING에서 계속 멈춤 | RTOS가 claim 후 finish하지 않음 | RTOS handler와 PATCH 호출 |
| FAILED | RTOS가 출력 실패를 보고함 | `result` 실패 사유 |
| COMPLETED인데 종이가 안 나옴 | RTOS가 잘못 성공 처리했을 수 있음 | 실제 프린터 제어 코드 |

RTOS가 꺼져 있어도 백엔드가 정상이라면 POST 응답은 PENDING으로 반환된다. 따라서 버튼이 완전히 반응하지 않거나 POST 자체가 실패한다면 RTOS보다 먼저 프론트 Request와 백엔드 로그를 확인해야 한다.

## 10. 현재 적용 상태 종합

| 기능 | 프론트엔드 | 백엔드 | RTOS | 최종 상태 |
| --- | --- | --- | --- | --- |
| 영수증 출력 버튼 endpoint | 연결 작업 중/현재 프론트 저장소에서 확인하지 못함 | POST endpoint 존재 | handler 확인 필요 | 실제 출력 미검증 |
| 영수증 Request | 현재 3필드 요청 필요 | eventType/payload/requestId 수신 | payload 계약 필요 | 임시 구조 |
| 주문번호 출력 버튼 endpoint | 연결 작업 중/현재 프론트 저장소에서 확인하지 못함 | POST endpoint 및 Service 적용 | 신규 handler 필요/확인 필요 | 실제 출력 미검증 |
| 주문번호 DB 조회 | 해당 없음 | 적용 | 해당 없음 | 백엔드 구현됨 |
| 주문번호 이벤트 생성 | 해당 없음 | `PRINT_WAITING_NUMBER`, payload 생성 | 같은 문자열 handler 필요 | 백엔드 구현됨 |
| eventId 반환 | 응답에서 저장 필요 | 적용 | finish 시 사용 | 백엔드 구현됨 |
| 출력 상태 GET | 프론트 polling 추가 필요 | 적용 | 완료 결과의 원천 | API 구현됨 |
| RTOS pending polling | 해당 없음 | 적용 | 실행 필요 | 실제 연동 미검증 |
| RTOS finish 보고 | 해당 없음 | 적용 | 실행 필요 | 실제 연동 미검증 |
| 이벤트 DB 저장 | 해당 없음 | 미적용 | 해당 없음 | 메모리 임시 저장 |

## 11. 현재 구현상 주의사항

1. `DeviceEventService`는 `ConcurrentHashMap` 메모리에 이벤트를 저장한다. Spring Boot를 재시작하면 모든 eventId와 PENDING 작업이 사라진다.
2. `requestId` 중복 검사가 없어 같은 버튼 요청이 여러 번 전달되면 중복 출력 이벤트가 생성될 수 있다.
3. 영수증 endpoint는 프론트가 eventType과 payload를 직접 보낸다. 서버가 주문 데이터와 일치하는지 검증하지 않는다.
4. 주문번호 조회 SQL의 `${orderId}`는 `#{orderId}` 바인딩 방식으로 변경하는 것이 필요하다.
5. 존재하지 않는 eventId 조회는 현재 `IllegalArgumentException`이 발생하고 공통 예외 처리에 의해 500으로 응답될 수 있다. 장치 이벤트 전용 404 ErrorCode가 필요하다.
6. RTOS polling과 finish API에 장치 인증이 아직 적용되지 않았다.
7. PROCESSING 상태에서 RTOS가 종료될 경우 timeout과 재시도 정책이 없다.
8. 현재 실제 RTOS 코드와 프린터 실행 결과는 확인하지 못했다.

## 12. 다음 작업 체크리스트

- [ ] 프론트 영수증 버튼에서 현재 Request 3필드 전송 확인
- [ ] 프론트 주문번호 버튼에서 `requestId` UUID 전송 확인
- [ ] POST 응답의 `eventId` 저장
- [ ] `deviceEvent(eventId)` 상태 polling 추가
- [ ] RTOS `PRINT_RECEIPT` handler 확인
- [ ] RTOS `PRINT_WAITING_NUMBER` handler 구현
- [ ] RTOS finish PATCH 호출 구현
- [ ] 영수증 payload 정식 구조 확정
- [ ] `${orderId}`를 `#{orderId}`로 변경
- [ ] `device_event` DB 영속화
- [ ] requestId 멱등성/중복 출력 방지
- [ ] RTOS 미실행, 용지 없음, 프린터 연결 해제 실패 테스트
- [ ] 실제 영수증과 주문번호 종이 출력 통합 테스트

## 13. 최종 요약

현재 백엔드는 주문번호 출력 요청을 받으면 프론트가 보낸 `requestId`와 URL의 `orderId`를 사용한다. DB에서 해당 주문의 `waiting_order_no`를 조회하고 `PRINT_WAITING_NUMBER`, payload=`"42"` 이벤트를 PENDING으로 생성한 뒤 eventId를 프론트에 반환한다.

영수증 출력 endpoint도 PENDING 이벤트를 생성하지만 현재는 프론트가 `PRINT_RECEIPT`, 영수증 payload, requestId를 모두 보내야 한다. 백엔드에서 영수증 데이터를 조회하고 만드는 구조는 아직 적용되지 않았다.

실제 종이 출력은 RTOS가 pending 이벤트를 pull하여 eventType별 handler를 실행해야 발생한다. POST 응답이 PENDING이면 백엔드 등록까지는 성공한 것이고, COMPLETED 또는 FAILED는 RTOS가 finish API로 결과를 보고한 뒤 확인할 수 있다.
