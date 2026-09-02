<div align="center">

# 🥗 ASAK Backend

**키오스크 주문부터 관리자 운영까지 하나의 서버에서 처리하는 ASAK API**

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.7-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-4.0.1-000000?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Status](https://img.shields.io/badge/Status-In_Development-F59E0B?style=flat-square)

[빠른 시작](#-빠른-시작) · [폴더 구조](#-프로젝트-폴더-구조도) · [API 목록](#-api-목록과-구현-상태) · [이미지](#️-메뉴-이미지--cloudinary-흐름) · [라이브러리](#-라이브러리-구성과-상세-역할) · [문서](#-관련-문서)

</div>

---

> **Team Project Original** — 팀 프로젝트 종료 시점 스냅샷
> Team Development: 2026.07 ~ 2026.09
> Freeze tag: `team-original-2026-09-02`
> 이 저장소에는 팀 종료 이후 개인 확장 작업을 추가하지 않습니다.

| 버전 | 배포 |
| --- | --- |
| Team Original (이 repo) | https://asak.stackroom.cloud |
| 하진 Personal Extension | https://hajin-asak.stackroom.cloud |
| 나연 Personal Extension | https://nayeon-asak.stackroom.cloud |

---

## 0. 프로젝트 한눈에 보기

본 프로젝트는 고객용 키오스크(User)와 관리자용 태블릿(Admin)을 **하나의 백엔드 서버**에서 처리하며, 모듈성과 유지보수성을 위해 도메인·역할별로 패키지를 분리해 관리합니다.

| 패키지 | 구분 | 담당 역할 |
| --- | --- | --- |
| `common` | 공통 모듈 | 시스템 전역 설정(Security, DB, Swagger), JWT 코드, 예외 처리, 공통 응답 |
| `admin` | 태블릿 (관리자) | 메뉴·옵션 관리, 주문 상태 변경, 품절·결제수단·매출 기능 |
| `user` | 키오스크 (고객) | 메뉴 조회, 장바구니 검증, 주문 접수, 결제 처리 |

처음 코드를 읽는다면 **`UserMenuController` → `UserOrderService` → `UserOrderMapper.xml`** 순서로 한 기능을 세로로 따라가 보는 것을 권합니다. 이 흐름 하나를 이해하면 나머지 기능도 같은 구조입니다.

---

## 🚀 빠른 시작

### 1단계: 환경 변수 준비

[`.env.example`](.env.example)을 복사해 본인 값으로 채웁니다. `application.properties`가 아래 세 값을 환경 변수로 읽습니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/asak_db?...
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

`.env.example`에는 Cloudinary 작업 도구용 `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `CLOUDINARY_URL` 형식도 있습니다. 이 값은 저장소에 커밋하지 않습니다.

### 2단계: 로컬 MySQL 준비

`asak_db` 스키마와 테이블·기초 데이터가 있어야 서버가 정상 동작합니다. 테이블 정의는 중앙 문서 `ASAK/docs/wiki/db-table-definition.md`를 참고합니다.

### 3단계: 서버 실행

```powershell
cd C:\ASAK-workspace\ASAK-back
.\gradlew.bat bootRun
```

### 4단계: 동작 확인

| 확인 항목 | 주소 |
| --- | --- |
| 서버 상태 | `GET http://localhost:8080/api/health` |
| Swagger UI (API 문서) | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

Swagger는 `SwaggerConfig`에서 **00. 공통 / 01. 키오스크 / 02. 관리자** 세 그룹으로 나눠 보여줍니다.

### 자주 쓰는 명령어

```powershell
.\gradlew.bat bootRun        # 개발 서버 실행
.\gradlew.bat test           # 테스트 실행
.\gradlew.bat build          # 빌드 (테스트 포함)
.\gradlew.bat spotlessApply  # 코드 포맷 자동 정리
.\gradlew.bat spotlessCheck  # 포맷 위반 검사
```

> ⚠️ 현재 작업 트리에는 관리자 옵션 그룹 구현 초안이 포함되어 있습니다. 빌드·실행 통과 여부는 작업 시점에 직접 확인하세요.

---

## 📁 프로젝트 폴더 구조도

```text
com.asak/
│
├── AsakBackendApplication.java      # [진입점] Spring Boot 애플리케이션 시작 클래스
│
├── common/                          # [공통 패키지] Admin과 User가 함께 쓰는 파일들
│   │
│   ├── config/                      # [설정] 프로젝트 전역 시스템 설정
│   │   ├── DatabaseConfig.java      # - DataSource 및 MyBatis/DB 연결 설정
│   │   ├── SecurityConfig.java      # - Security/CORS/인증 허용 경로 (현재 전체 임시 허용)
│   │   ├── WebMvcConfig.java        # - 정적 리소스 경로, 인터셉터 설정
│   │   └── SwaggerConfig.java       # - Swagger UI 그룹(공통/키오스크/관리자) 설정
│   │
│   ├── controller/                  # [공통 컨트롤러]
│   │   └── HealthController.java    # - GET /api/health 서버 상태 확인
│   │
│   ├── exception/                   # [예외 처리] 서버 에러를 한곳에서 관리
│   │   ├── GlobalExceptionHandler.java # - 발생한 예외를 공통 응답 양식으로 변환
│   │   ├── CustomException.java     # - 비즈니스 로직에서 직접 던지는 예외
│   │   └── ErrorCode.java           # - enum (MENU_NOT_FOUND, MENU_SOLD_OUT 등)
│   │
│   ├── response/                    # [공통 응답] 프론트로 리턴하는 데이터 규격
│   │   ├── ApiResponse.java         # - success/status/code/message/data 공통 DTO
│   │   └── PageResult.java          # - 목록·페이징 응답 (content, page, size, total)
│   │
│   ├── enums/                       # [공통 상태값] API와 DB가 함께 쓰는 코드 정의
│   │   ├── OrderStatus.java         # - READY, RECEIVED, PREPARING, COMPLETED, CANCELED
│   │   ├── OrderType.java           # - EAT_IN, TAKE_OUT
│   │   ├── PaymentStatus.java       # - READY, APPROVED, FAILED, CANCELED, REFUNDED
│   │   ├── PaymentMethod.java       # - CARD, KAKAO_PAY, NAVER_PAY 등 결제수단
│   │   ├── OptionGroupType.java     # - 옵션 그룹 종류
│   │   ├── MenuIngredientRole.java  # - 메뉴 안에서 재료가 하는 역할
│   │   ├── IngredientType.java      # - 재료 분류
│   │   ├── UnitType.java            # - 수량 단위
│   │   └── TagType.java             # - 메뉴 태그 분류
│   │
│   ├── security/                    # [보안/인증] JWT 관련 (아직 필터 체인 미적용)
│   │   ├── JwtTokenProvider.java    # - 토큰 생성·파싱·검증 유틸
│   │   ├── JwtAuthenticationFilter.java # - 요청 헤더의 토큰 검사 필터
│   │   └── CustomUserDetails.java   # - Security가 인식하는 인증 사용자 객체
│   │
│   ├── util/                        # [공통 유틸리티]
│   │   ├── FileUtil.java            # - 레거시 로컬 파일 처리 유틸 (Cloudinary 업로드 API 아님)
│   │   └── DateUtil.java            # - 날짜·시간 포맷 변환
│   │
│   └── entity/                      # [공통 엔티티]
│       └── BaseEntity.java          # - created_at, updated_at 공통 필드
│
├── user/                            # [고객 전용] 키오스크 프론트엔드와 통신
│   │
│   ├── controller/                  # [컨트롤러] 키오스크 API 요청 수신
│   │   ├── UserMenuController.java  # - 카테고리, 메뉴 목록, 메뉴 상세 조회
│   │   ├── UserOrderController.java # - 장바구니 검증(API-004), 주문 생성(API-005)
│   │   └── UserPayController.java   # - 결제수단 조회(API-014), 결제 승인(API-006)
│   │
│   ├── service/                     # [비즈니스 로직] 키오스크 실제 처리
│   │   ├── UserMenuService.java     # - 판매 중인 메뉴만 필터링해 조회
│   │   ├── UserOrderService.java    # - 품절·옵션 검증, 서버 금액 계산, 주문번호 생성
│   │   └── UserPayService.java      # - 결제 승인, 중복 결제(멱등키) 처리
│   │
│   ├── dto/                         # [데이터 전달 객체] 기능별로 세분화
│   │   ├── menu/                    # - CategoryResponse, MenuListResponse, MenuDetailResponse 등
│   │   ├── order/request/           # - CartValidateRequest, CreateOrderRequest, OptionItemRequest
│   │   ├── order/response/          # - CartValidateResponse, CreateOrderResponse
│   │   ├── order/internal/          # - 서버 내부 계산·INSERT 전용 (ValidatedOrderItem 등)
│   │   ├── order/query/             # - Mapper 조회 결과 (MenuQueryDto, OptionPolicyQueryDto)
│   │   └── payment/                 # - ApprovePaymentRequest/Response, PaymentMethodListResponse
│   │
│   └── mapper/                      # [MyBatis 매퍼] 키오스크 전용 DB 쿼리
│       ├── UserMenuMapper.java      # - 노출 가능한 메뉴·옵션 SELECT
│       ├── UserOrderMapper.java     # - 주문 INSERT, 메뉴·옵션 검증 조회
│       └── UserPayMapper.java       # - 결제 INSERT, 결제수단·멱등키 조회
│
└── admin/                           # [관리자 전용] 태블릿 프론트엔드와 통신
    │
    ├── controller/                  # [컨트롤러] 관리자 API 요청 수신
    │   ├── AdminMenuController.java # - 메뉴 목록·상세·등록·수정·삭제, 카테고리·재료 조회
    │   ├── AdminOptionController.java # - 옵션 그룹 목록·상세 조회
    │   ├── AdminOrderController.java# - 주문 목록·상세, Live 보드, 상태 변경·취소
    │   ├── AdminAuthController.java # - 관리자 로그인 (TODO-027 미구현)
    │   ├── AdminSoldOutController.java # - 품절 관리 (TODO-007 미구현)
    │   ├── AdminPaymentMethodController.java # - 결제수단 설정 (TODO-011 미구현)
    │   └── AdminStatsController.java# - 매출·대시보드 집계 (TODO-015~023 미구현)
    │
    ├── service/                     # [비즈니스 로직] 관리자 실제 처리
    │   ├── AdminMenuService.java    # - 메뉴 등록·수정·soft delete, 참조 존재 검증
    │   ├── AdminOptionService.java  # - 옵션 그룹 조회·검증
    │   ├── AdminOrderService.java   # - 주문 목록·상세 조립, 상태 전이 처리
    │   ├── AdminSoldOutService.java # - 품절 처리 (구현 진행 대상)
    │   ├── AdminPaymentMethodService.java # - 결제수단 설정 (구현 진행 대상)
    │   └── AdminStatsService.java   # - 매출 집계 (구현 진행 대상)
    │
    ├── dto/                         # [데이터 전달 객체]
    │   ├── request/                 # - MenuListRequest, CreateMenuRequest, UpdateMenuRequest,
    │   │                            #   OrderListFilter, SoldOutPatchRequest 등
    │   └── response/                # - MenuListResponse, MenuDetailResponse, OrderListResponse,
    │                                #   OrderDetailResponse, LiveOrderResponse 등
    │
    └── mapper/                      # [MyBatis 매퍼] 관리자 전용 DB 쿼리
        ├── AdminMenuMapper.java     # - 메뉴·카테고리·재료 조회 및 등록/수정/삭제
        ├── AdminOptionMapper.java   # - 옵션 그룹·옵션 항목 조회
        ├── AdminOrderMapper.java    # - 주문 목록·상세·Live·상태 변경
        ├── AdminSoldOutMapper.java  # - 품절 대상 조회·변경
        ├── AdminPaymentMethodMapper.java # - 결제수단 조회·변경
        └── AdminStatsMapper.java    # - 매출·대시보드 집계 쿼리

src/main/resources/
├── application.properties           # DB 연결, MyBatis, 로깅 등 서버 설정
└── mappers/                         # 실제 SQL문이 들어 있는 XML 폴더
    ├── UserMenuMapper.xml
    ├── UserOrderMapper.xml
    ├── UserPayMapper.xml
    ├── AdminMenuMapper.xml
    ├── AdminOptionMapper.xml
    ├── AdminOrderMapper.xml
    ├── AdminSoldOutMapper.xml
    ├── AdminPaymentMethodMapper.xml
    └── AdminStatsMapper.xml
```

---

## 🔌 API 목록과 구현 상태

경로는 실제 Controller의 `@RequestMapping`·`@GetMapping` 등을 그대로 옮긴 것입니다. **문서보다 Controller mapping이 구현 상태의 직접 근거**입니다.

### 공통

| Method | 경로 | 설명 | 상태 |
| --- | --- | --- | --- |
| GET | `/api/health` | 서버 상태 확인 | ✅ 구현됨 |

### 키오스크 (`UserMenuController`, `UserOrderController`, `UserPayController`)

| API | Method | 경로 | 설명 | 상태 |
| --- | --- | --- | --- | --- |
| API-001 | GET | `/api/kiosk/categories` | 카테고리 탭 목록 | ✅ 구현됨 |
| API-002 | GET | `/api/kiosk/menuList` | 메뉴 카드 목록 | ✅ 구현됨 |
| API-003 | GET | `/api/kiosk/menuDetail/{menuId}` | 재료·알레르기·옵션·품절 정보 | ✅ 구현됨 |
| API-004 | POST | `/api/kiosk/cart/validate` | 주문 직전 가격·필수 옵션·품절 재검증 | ✅ 구현됨 |
| API-005 | POST | `/api/kiosk/orders` | 주문 생성 (서버가 `totalAmount` 계산) | ✅ 구현됨 |
| API-014 | GET | `/api/kiosk/payment-methods` | 결제수단 목록 | ✅ 구현됨 |
| API-006 | POST | `/api/kiosk/payments` | 가상 결제 승인 | ✅ 구현됨 |

> 주문 요청에는 클라이언트가 금액을 보내지 않습니다. `orderType`, `items[].menuId`, `quantity`, `optionItems[].optionItemId`, `optionItems[].quantity`, `excludedIngredientIds`만 보내고 금액은 **항상 서버가 DB 가격으로 계산**합니다.

### 관리자 — 구현된 API

| API | Method | 경로 | 설명 |
| --- | --- | --- | --- |
| API-007 | GET | `/api/admin/orders` | 주문 목록 (상태·유형·기간·검색·페이징) |
| API-022 | GET | `/api/admin/orders/{orderId}` | 주문 상세 |
| API-021 | GET | `/api/admin/orders/live` | Live 주문 보드 (대기·조리 중) |
| API-008 | PATCH | `/api/admin/orders/{orderId}/{status}` | 주문 상태 변경 |
| API-024 | PATCH | `/api/admin/orders/{orderId}/cancel` | 주문 취소·환불 처리 |
| API-011 | GET | `/api/admin/menus` | 메뉴 목록 (카테고리·검색·품절·태그·페이징) |
| API-023 | GET | `/api/admin/menus/{menuId}` | 메뉴 상세 |
| API-012 | POST | `/api/admin/menus` | 메뉴 등록 (JSON body) |
| API-013 | PATCH | `/api/admin/menus/{menuId}` | 메뉴 수정 |
| — | DELETE | `/api/admin/menus/{menuId}` | 메뉴 삭제 (soft delete · 주문 이력 유지) |
| — | GET | `/api/admin/menus/categories` | 카테고리 목록 (보조 조회) |
| — | GET | `/api/admin/menus/ingredients` | 재료 목록 (보조 조회) |
| — | GET | `/api/admin/opts/groups` | 옵션 그룹 목록 (보조 조회) |
| — | GET | `/api/admin/opts/{optionGroupId}` | 옵션 그룹 상세 (보조 조회) |

`—` 표시는 `IMPLEMENTATION_PLAN.md` §4의 API 번호 목록에 없는 보조·추가 endpoint입니다.

### 관리자 — 아직 비어 있는 Controller

| API | Controller | 기본 경로 | 남은 작업 |
| --- | --- | --- | --- |
| API-009 / 010 | `AdminSoldOutController` | `/api/admin/soldOut` | TODO-007 품절 조회·변경 |
| API-015 / 016 | `AdminPaymentMethodController` | `/api/admin/paymentMethods` | TODO-011 결제수단 목록·설정 |
| API-017 / 018 / 019 | `AdminStatsController` | `/api/admin/sales/**` | TODO-015~017 일별·요약·월별 매출 |
| API-020 | `AdminStatsController` | `/api/admin/dashboard` | TODO-023 대시보드 집계 |
| — | `AdminAuthController` | `/api/admin/login` | TODO-027 로그인·JWT 발급 |

> ⚠️ API-015/016 경로는 현재 코드가 camelCase(`/api/admin/paymentMethods`)이고 Product Bible은 kebab-case(`/api/admin/payment-methods`)입니다. 프론트 연결 전에 정본을 하나로 확정해야 합니다.

클래스는 존재하지만 mapping이 없으므로 **호출되지 않습니다.** 프론트에서 mock을 쓰고 있는 화면(품절·결제수단·매출·대시보드)이 여기에 대응합니다.

---

## 🖼️ 메뉴 이미지 · Cloudinary 흐름

메뉴 이미지는 프론트 저장소의 `public/assets/menu`를 정본으로 두는 방식에서 **Cloudinary URL을 가진 `media_asset` 레코드**를 참조하는 방식으로 전환했습니다.

### 최종 데이터 흐름

```text
Cloudinary에 이미지 업로드
  → DB media_asset에 provider·public_id·url 저장
  → menu.image_asset_id가 media_asset.id 참조
  → UserMenuMapper / AdminMenuMapper가 media_asset.url JOIN
  → API가 기존 필드명 imageUrl로 응답
  → Kiosk / Admin의 <img src={imageUrl}>가 클라우드 이미지를 표시
```

프론트가 받는 필드명은 계속 `imageUrl`입니다. 바뀐 것은 **URL의 저장 책임**입니다.

| 구분 | 과거 | 현재 |
| --- | --- | --- |
| 이미지 원본 위치 | 프론트 `public/assets/menu/**` | Cloudinary |
| 메뉴 테이블 | `menu.image_url` 문자열 직접 저장 | `menu.image_asset_id` FK 저장 |
| URL 정본 | 메뉴 행의 문자열 | `media_asset.url` |
| API 응답 | `imageUrl` | `imageUrl` (필드명 유지) |
| 프론트 수정 | 로컬 경로 사용 | API URL을 그대로 `<img src>`에 사용 |

### 현재 코드에서 확인할 파일

| 파일 | 역할 |
| --- | --- |
| `CreateMenuRequest.java` | 새 클라이언트가 보내는 `mediaAssetId`와 레거시 `imageUrl` 호환 필드 |
| `AdminMenuService.java` | `mediaAssetId`가 활성 자산인지 검증하고, 레거시 URL은 활성 자산 ID로 변환 |
| `AdminMenuMapper.xml` | 메뉴 등록·수정 시 `image_asset_id` 저장, 관리자 응답에서 `media_asset.url` JOIN |
| `UserMenuMapper.xml` | 키오스크 메뉴 목록·상세에서 `media_asset.url AS imageUrl` 조회 |
| `.env.example` | Cloudinary 자격 증명 변수 형식 (실제 비밀값 커밋 금지) |
| `docs/menu-image-asset-flow.md` | 전환 근거·데이터 보정·검증 절차 상세 |

### 중요한 경계

- 백엔드 서버에는 아직 **Cloudinary 업로드 API가 없습니다.** 업로드와 `media_asset` 생성은 별도 작업입니다.
- 관리자 프론트는 아직 `mediaAssetId` 선택 UI가 없고 `imageUrl` 호환 경로를 사용합니다.
- `image_asset_id`가 없거나 연결된 자산이 soft delete 상태면 API의 `imageUrl`은 `null`일 수 있습니다.
- `.env.example`에 Cloudinary 변수가 있다는 사실만으로 업로드·DB 연결·화면 표시가 모두 검증된 것은 아닙니다.
- 과거 로컬 정적 경로와 Cloudinary URL이 다르면 자동 보정되지 않으므로, 미연결 메뉴는 `media_asset`을 확인해 수동 연결해야 합니다.

---

## 🔐 공통 응답 규격과 보안

### 응답 Envelope

모든 신규 API는 아래 형태로 응답합니다.

```json
{
  "success": true,
  "status": 200,
  "code": "ADMIN_MENU_LIST_SUCCESS",
  "message": "관리자 메뉴 목록 조회 성공",
  "data": {}
}
```

`code`는 API별 의미 있는 문자열입니다. 레거시 숫자 코드(`0000`, `2001`)는 사용하지 않습니다.

### 필드 이름 규칙 (DB → API)

| DB 컬럼 | API 필드 |
| --- | --- |
| `orders.total_price` | `totalAmount` |
| `payment.amount` | `approvedAmount` |
| `payment.paid_at` | `approvedAt` |
| `menu.cat_id` | `categoryId` |
| `menu.sold_out` | `isSoldOut` |
| `category.sort_no` | `sortOrder` |

Java와 JSON은 camelCase, DB 컬럼은 snake_case를 씁니다.

### 보안 현재 상태

`SecurityConfig`는 JWT 전환 전 단계로 **모든 요청을 임시 허용**(`anyRequest().permitAll()`)하고, CORS는 모든 origin을 허용합니다. 세션은 `STATELESS`이고 `PasswordEncoder`와 `AuthenticationManager` Bean만 준비된 상태입니다.

> ⚠️ 인증이 완료된 상태로 간주하면 안 됩니다. 배포 전에 `/api/admin/login`은 `permitAll`, 그 외 `/api/admin/**`은 `authenticated`로 전환하고 허용 origin을 제한해야 합니다. (TODO-030)

---

## 🧭 구현 원칙

API 하나를 만들 때는 아래 순서로 **세로로 완성**합니다.

```text
Controller → Service → Mapper interface → Mapper XML → MySQL → Response DTO
```

| 계층 | 담당 |
| --- | --- |
| Controller | HTTP mapping, 요청·응답 경계, `@Valid` 검증 |
| Service | 요청 검증, 트랜잭션, 금액 계산, 상태 전이 |
| Mapper + XML | 조회·필터·조인·집계 SQL |
| DTO | 기능별 `dto/request`, `dto/response` 분리 |

- Mapper XML은 `src/main/resources/mappers/**`에 둡니다.
- 새 API 경로는 `/api/kiosk/**`, `/api/admin/**`만 사용합니다.
- 레거시 `/api/menus`, `/api/orders`, `/api/payments`, `/api/v1/**`는 새 Controller에 복사하지 않습니다.
- 빈 `request.java`, `response.java` 클래스는 만들지 않습니다.

---

## 🛠️ 라이브러리 구성과 상세 역할

프로젝트에 추가된 라이브러리(`build.gradle`) 목록과 각각의 동작 방식입니다.

### 1) Spring Web MVC (`spring-boot-starter-webmvc`)

**역할:** RESTful API 웹 애플리케이션 구축을 위한 핵심 라이브러리입니다.

**동작 방식:** 내장 Tomcat 서버를 구동하고, `@RestController`, `@GetMapping`, `@PostMapping` 등의 어노테이션으로 HTTP 요청을 적절한 자바 메서드에 매핑합니다. JSON 변환을 위해 Jackson을 기본 제공합니다.

### 2) Spring Security (`spring-boot-starter-security`)

**역할:** 인증(Authentication)과 인가(Authorization)를 담당하는 보안 프레임워크입니다.

**동작 방식:** 서블릿 필터 체인 기반으로 모든 HTTP 요청을 백엔드 로직 전에 가로챕니다. 현재는 JWT 구현 전이라 전체 허용 상태이며, 이후 관리자/고객 권한에 맞춰 접근을 제한합니다.

### 3) Spring Data JPA (`spring-boot-starter-data-jpa`)

**역할:** 자바 객체(Entity)와 관계형 DB 테이블을 매핑하는 ORM 기술입니다.

**동작 방식:** 기본 CRUD를 객체 단위로 처리할 수 있게 해줍니다. 다만 ASAK는 **조회·집계 SQL을 MyBatis로 직접 작성**하는 방식을 택했으므로, JPA는 의존성으로 포함되어 있고 실제 데이터 접근의 중심은 MyBatis Mapper입니다.

### 4) MyBatis (`mybatis-spring-boot-starter`)

**역할:** 자바 객체와 SQL문 사이를 매핑하는 SQL Mapper 프레임워크입니다. **ASAK의 주 데이터 접근 수단입니다.**

**동작 방식:** Mapper 인터페이스와 XML의 실제 SQL을 1:1로 매핑합니다. 메뉴 상세 조립, 주문 검증 조회, 매출 통계처럼 조인·집계·동적 조건이 많은 쿼리를 직접 작성할 때 사용합니다.

### 5) MySQL Driver (`mysql-connector-j`)

**역할:** 자바 애플리케이션과 MySQL을 물리적으로 연결하는 JDBC 드라이버입니다.

**동작 방식:** DB 커넥션을 맺고 SQL을 MySQL 서버에 전송하며, 결과를 자바 데이터 타입으로 받아옵니다.

### 6) Validation (`spring-boot-starter-validation`)

**역할:** DTO로 들어오는 데이터의 유효성 검증을 수행합니다.

**동작 방식:** Controller에서 `@Valid`를 붙이면 DTO 필드의 `@NotBlank`, `@NotNull`, `@Size` 등을 검사합니다. 필수값 누락이나 규칙 위반 시 비즈니스 로직 실행 전에 잘못된 요청을 차단합니다.

### 7) Lombok

**역할:** 반복되는 Boilerplate 코드를 줄여주는 편의 도구입니다.

**동작 방식:** 컴파일 시점에 `@Getter`, `@Builder`, `@RequiredArgsConstructor` 등의 어노테이션을 보고 Getter/Setter/생성자를 바이트코드로 자동 생성합니다. ASAK에서는 응답 DTO의 `@Builder`와 Service의 생성자 주입에 많이 쓰입니다.

### 8) Springdoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`)

**역할:** Swagger UI를 생성해 브라우저에서 API 명세를 확인·테스트할 수 있게 합니다.

**동작 방식:** Controller 매핑을 스캔해 OpenAPI 문서를 만듭니다. `SwaggerConfig`에서 경로 패턴으로 공통·키오스크·관리자 그룹을 나눠 보여줍니다.

### 9) JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)

**역할:** JWT(JSON Web Token)를 생성·파싱·검증합니다.

**동작 방식:** `common/security` 패키지의 `JwtTokenProvider`가 이 라이브러리로 토큰을 만들고 검증합니다. 관리자 로그인(TODO-027)이 구현되면 실제로 사용됩니다.

### 10) Spring Boot DevTools

**역할:** 개발 생산성을 높이는 도구입니다.

**동작 방식:** 클래스·리소스 변경을 감지해 서버를 빠르게 자동 재시작합니다.

### 11) Spring Configuration Processor

**역할:** `application.properties`의 자바 매핑 클래스 메타데이터를 생성합니다.

**동작 방식:** `@ConfigurationProperties`를 쓸 때 IDE가 커스텀 설정 키를 인식하도록 만들어 자동 완성과 타입 힌트를 제공합니다.

### 12) Spotless (`com.diffplug.spotless`)

**역할:** 코드 포맷을 팀 전체에서 통일합니다.

**동작 방식:** `googleJavaFormat` 규칙으로 `src/*/java/**/*.java`를 검사·정리합니다. 커밋 전 `spotlessApply`를 실행하면 포맷 차이로 인한 diff를 줄일 수 있습니다.

---

## 📚 관련 문서

| 문서 | 내용 |
| --- | --- |
| [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md) | API 정본 목록, 필드 규칙, 구현 순서 |
| [`api/README.md`](api/README.md) | Bruno 컬렉션 사용법과 요청 형식 |
| [`docs/guides/README.md`](docs/guides/README.md) | 저장소 보조 가이드 목차 |
| [`docs/implementation-guide/00-start-here.md`](docs/implementation-guide/00-start-here.md) | 구현 가이드 시작점 |
| [`docs/implementation-guide/04-api-db-implementation.md`](docs/implementation-guide/04-api-db-implementation.md) | API·DB 구현 규칙 |
| [`docs/implementation-guide/08-feature-implementation-matrix.md`](docs/implementation-guide/08-feature-implementation-matrix.md) | 기능별 구현 매트릭스 |
| [`docs/menu-image-asset-flow.md`](docs/menu-image-asset-flow.md) | Cloudinary·`media_asset` 메뉴 이미지 전환 흐름 |

제품 정책·화면 계약의 정본은 워크스페이스의 `ASAK/docs/product_bible/`입니다. 코드와 문서가 다르면 한쪽을 조용히 맞추지 말고 불일치로 기록한 뒤 팀에서 결정합니다.
