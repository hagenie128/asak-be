# ASAK-backend
# 📂 Project Directory Structure & Library Specification

본 프로젝트는 고객용 키오스크(User)와 관리자용 태블릿(Admin)을 하나의 백엔드 서버에서 처리하며, 모듈성과 유지보수성을 위해 도메인/역할별로 패키지를 분리하여 관리합니다.

---
## 0.
```text
패키지,구분,담당 역할
common,공통 모듈,"시스템 전역 설정(Security, DB), JWT 인증, 예외 처리 및 공통 응답 처리"
admin,태블릿 (관리자),"메뉴 등록/수정/삭제, 주문 상태 변경, 매출 통계 분석 등 관리 기능"
user,키오스크 (고객),"메뉴 목록 조회, 주문 접수 및 결제 처리 등 실시간 사용자 기능"



## 1. 📁 프로젝트 폴더 구조도

```text
com.asak/
│
├── common/                          # [공통 패키지] Admin과 User 모두에서 공유하여 사용하는 파일들
│   │
│   ├── config/                      # [설정] 프로젝트 전역 시스템 설정 클래스
│   │   ├── DatabaseConfig.java      # - DataSource 및 MyBatis/DB 연결 설정
│   │   ├── SecurityConfig.java      # - Spring Security 보안/CORS/인증 허용 경로 설정
│   │   ├── WebMvcConfig.java        # - 정적 리소스(이미지 등) 접근 경로, 인터셉터 설정
│   │   └── SwaggerConfig.java       # - API 명세서(Swagger UI) 그룹화 및 설정
│   │
│   ├── exception/                   # [예외 처리] 서버에서 발생하는 모든 에러를 공통 관리
│   │   ├── GlobalExceptionHandler.java # - 발생한 에러를 감지하여 공통 양식으로 응답해주는 클래스
│   │   ├── CustomException.java     # - 비즈니스 로직 중 직접 발생시킬 예외 클래스
│   │   └── ErrorCode.java           # - enum 파일 (예: USER_NOT_FOUND, STOCK_EMPTY 등 에러코드 모음)
│   │
│   ├── response/                    # [공통 응답] 프론트엔드로 리턴할 데이터 규격
│   │   ├── ApiResponse.java         # - 성공/실패 여부, 상태코드, 메시지, 데이터(data)를 담는 공통 DTO
│   │   └── PageResult.java          # - 목록/페이징 조회 시 (현재 페이지, 전체 개수 등) 담는 DTO
│   │
│   ├── security/                    # [보안/인증] JWT 토큰 발행 및 검증 관련
│   │   ├── JwtTokenProvider.java    # - JWT 토큰 생성, 암호화, 파싱, 유효성 검증 유틸
│   │   ├── JwtAuthenticationFilter.java # - 클라이언트 요청의 헤더에서 토큰을 검사하는 필터
│   │   └── CustomUserDetails.java   # - Security에서 인식하는 인증 사용자 객체
│   │
│   ├── util/                        # [공통 유틸리티] 자주 쓰이는 도구성 함수 모음
│   │   ├── FileUtil.java            # - 메뉴/카테고리 이미지 파일 업로드 및 삭제
│   │   └── DateUtil.java            # - 날짜/시간 포맷 변환 (예: YYYY-MM-DD HH:mm:ss)
│   │
│   └── entity/                      # [공통 엔티티/도메인]
│       └── BaseEntity.java          # - 모든 DB 테이블에 들어가는 created_at(생성일), updated_at(수정일) 공통 클래스
│
│
├── admin/                           # [관리자 전용 패키지] 태블릿 프론트엔드와 통신하는 백엔드 로직
│   │
│   ├── controller/                  # [컨트롤러] 관리자용 API 엔드포인트 요청을 받는 곳
│   │   ├── AdminOrderController.java# - 관리자 주문 관리 (주문 승인, 조리완료, 취소 등)
│   │   ├── AdminMenuController.java # - 관리자 메뉴 관리 (메뉴 등록, 수정, 삭제, 품절 처리)
│   │   └── AdminStatsController.java# - 매출 통계 및 분석 API
│   │
│   ├── service/                     # [비즈니스 로직] 관리자의 실제 기능 실행 로직
│   │   ├── AdminOrderService.java   # - 주문 상태 변경 로직, 알림 처리 등
│   │   └── AdminMenuService.java    # - 메뉴 등록/수정 시 파일 저장 및 DB 업데이트
│   │
│   ├── dto/                         # [데이터 전달 객체] 관리자 화면과 데이터 주고받을 때 쓰는 객체
│   │   ├── request/                 # - 관리자가 보내는 데이터 (예: MenuCreateRequest, OrderStatusUpdateRequest)
│   │   └── response/                # - 관리자에게 돌려주는 데이터 (예: DailySalesResponse, AdminMenuDetailResponse)
│   │
│   └── mapper/                      # [MyBatis 매퍼] 관리자 전용 DB 쿼리 연결
│       ├── AdminOrderMapper.java    # - Java 인터페이스
│       ├── AdminMenuMapper.java     # - Java 인터페이스
│       └── xml/                     # - 실제 SQL문이 들어있는 XML 파일 폴더
│           ├── AdminOrderMapper.xml
│           └── AdminMenuMapper.xml
│
│
└── user/                            # [고객 전용 패키지] 키오스크 프론트엔드와 통신하는 백엔드 로직
    │
    ├── controller/                  # [컨트롤러] 키오스크 고객용 API 요청을 받는 곳
    │   ├── UserMenuController.java  # - 고객용 메뉴 목록 조회, 카테고리별 조회
    │   ├── UserOrderController.java # - 키오스크 주문 접수 및 장바구니 결제
    │   └── UserPayController.java   # - PG사 연동/결제 요청 처리
    │
    ├── service/                     # [비즈니스 로직] 키오스크 관련 실제 처리 로직
    │   ├── UserMenuService.java     # - 판매 중인 메뉴만 필터링하여 조회하는 로직
    │   └── UserOrderService.java    # - 재고 확인, 주문 생성, 결제 검증 로직
    │
    ├── dto/                         # [데이터 전달 객체] 키오스크 화면과 데이터 주고받을 때 쓰는 객체
    │   ├── request/                 # - 키오스크가 보내는 데이터 (예: CreateOrderRequest, PayVerifyRequest)
    │   └── response/                # - 키오스크에 보여줄 데이터 (예: UserMenuListResponse, OrderReceiptResponse)
    │
    └── mapper/                      # [MyBatis 매퍼] 키오스크 전용 DB 쿼리 연결
        ├── UserMenuMapper.java      # - Java 인터페이스 (예: 노출 가능한 메뉴만 SELECT)
        ├── UserOrderMapper.java     # - Java 인터페이스 (예: 주문 INSERT)
        └── xml/                     # - 실제 SQL문이 들어있는 XML 파일 폴더
            ├── UserMenuMapper.xml
            └── UserOrderMapper.xml


3. 🛠️ 라이브러리 구성 및 상세 역할 설명
프로젝트 생성 시 추가된 라이브러리(build.gradle) 목록과 각각의 동작 방식 및 역할 설명입니다.

1) Spring Web (spring-boot-starter-web)
역할: RESTful API 웹 애플리케이션 구축을 위한 핵심 라이브러리입니다.

동작 방식: 내장된 Tomcat 서버를 구동하고, @RestController, @GetMapping, @PostMapping 등의 어노테이션을 통해 HTTP 요청을 받아 적절한 자바 메소드로 매핑해 줍니다. 또한 JSON 데이터 변환을 위해 Jackson 라이브러리를 기본 제공합니다.

2) Spring Security (spring-boot-starter-security)
역할: 인증(Authentication) 및 인가(Authorization)를 담당하는 보안 프레임워크입니다.

동작 방식: 서블릿 필터 체인(Filter Chain) 기반으로 작동하여 모든 HTTP 요청이 백엔드로 들어오기 전 가로채기(Interception)를 수행합니다. 인증되지 않은 사용자의 특정 API 접근을 차단하고, 관리자/고객 권한에 맞춰 접근을 제한합니다.

3) Spring Data JPA (spring-boot-starter-data-jpa)
역할: 자바 객체(Entity)와 관계형 데이터베이스(RDB) 테이블을 매핑해주는 ORM(Object-Relational Mapping) 기술입니다.

동작 방식: SQL을 직접 작성하지 않고 자바 코드로 데이터베이스 조작이 가능하도록 지원합니다. 복잡한 쿼리 외의 기본적인 CRUD(저장, 수정, 삭제, 조회)를 객체 단위로 깔끔하게 처리해 줍니다.

4) MyBatis Framework (mybatis-spring-boot-starter)
역할: 자바 객체와 SQL문 사이를 매핑해주는 SQL Mapper 프레임워크입니다.

동작 방식: 자바 코드 인터페이스(Mapper)와 XML 파일에 적힌 실제 SQL문을 1:1로 매핑시켜 줍니다. 데이터베이스 복잡도가 높은 집계 쿼리(예: 관리자용 매출 통계)나 동적 SQL을 직접 작성할 때 사용됩니다.

5) MySQL Driver (mysql-connector-j)
역할: 자바 애플리케이션과 MySQL 데이터베이스를 물리적으로 연결해 주는 JDBC 드라이버입니다.

동작 방식: 백엔드 서버가 DB 커넥션을 맺고, 작성된 SQL 명령어를 MySQL 서버에 전송하며, 실행 결과 데이터를 자바 데이터 타입으로 전달받을 수 있게 해주는 교량 역할을 합니다.

6) Validation (spring-boot-starter-validation)
역할: DTO 등 자바 객체로 들어오는 데이터의 유효성 검증(Validation)을 수행합니다.

동작 방식: 컨트롤러에서 @Valid 어노테이션을 붙이면 DTO 내부 필드에 선언된 @NotBlank, @NotNull, @Size 등의 어노테이션을 검사합니다. 필수값 누락이나 규칙 위반 시 예외를 발생시켜 비즈니스 로직 실행 전 잘못된 요청을 선제 차단합니다.

7) Lombok (projectlombok)
역할: 자바 코드 작성 시 반복적으로 발생하는 Boilerplate(기본 반복 코드)를 줄여주는 편의 도구입니다.

동작 방식: 컴파일 시점에 자바 AST(Abstract Syntax Tree)를 수정하여 @Getter, @Setter, @NoArgsConstructor, @Builder 등의 어노테이션만 작성해두면 자동으로 Getter/Setter/생성자 코드를 바이트코드로 합성해 줍니다.

8) Spring Boot DevTools (spring-boot-devtools)
역할: 개발 효율성을 올려주는 생산성 향상 도구입니다.

동작 방식: 자바 클래스 파일이나 리소스 파일 변경을 감지하여 서버를 완전히 다시 시작하는 대신, 빠르게 자동 리로드(Automatic Restart)를 실행해 개발 중 서버 재시작 시간을 단축해 줍니다.

9) Spring Configuration Processor (spring-boot-configuration-processor)
역할: application.yml 또는 properties 파일의 자바 매핑 클래스에 대한 메타데이터를 생성합니다.

동작 방식: @ConfigurationProperties 어노테이션을 사용할 때, IDE가 커스텀 설정 키값을 인식하도록 만들어 줍니다. 설정 파일 작성 시 자동 완성(Auto-completion) 기능과 타입 힌트를 제공합니다.

💡 [추가 라이브러리] 프로젝트 내 포함된 기능
Springdoc OpenAPI (springdoc-openapi-starter-webmvc-ui): Swagger UI를 생성하여 프론트엔드 개발자가 웹 브라우저에서 백엔드 API 명세서를 직접 확인하고 테스트할 수 있게 해줍니다.

JJWT (jjwt-api, jjwt-impl, jjwt-jackson): JWT(JSON Web Token)를 생성 및 파싱하고 검증하는 용도로 사용되는 라이브러리입니다.