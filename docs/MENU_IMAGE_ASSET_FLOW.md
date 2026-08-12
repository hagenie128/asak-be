# 메뉴 이미지 경로 사용 이력과 `media_asset` 전환 기준

> 작성일: 2026-08-13
> 대상: 관리자 메뉴 관리 API, 고객 키오스크 메뉴 조회 API
> 상태: 코드 반영 · 실제 DB 보정 SQL 실행 및 조회 API 실연동 검증은 별도

## 1. 왜 처음에는 공개 URL 경로를 `menu.image_url`에 넣었나

초기에는 별도 이미지 서버나 공용 백엔드 서버가 없었고, 여러 PC를 오가며 프런트 작업을 해야 했다. 그래서 메뉴 이미지를 **프런트 프로젝트의 `public` 에셋으로 관리**하고, 어느 PC에서 Vite를 실행해도 같은 URL로 보이게 했다.

메뉴 하나에 이미지 주소 하나면 충분했으므로 그 프런트 에셋 URL 문자열을 `menu.image_url`에 직접 저장하고, 고객·관리자 화면에는 같은 값을 `imageUrl`로 반환했다.

예시:

```text
/assets/menu/364.png
```

이 방식은 MVP 단계에서 다음 장점이 있었다.

- 각 PC에 프런트 저장소만 있으면 메뉴 이미지를 함께 실행할 수 있다.
- 별도 서버·공유 스토리지·업로드 API를 준비하지 않아도 된다.
- 메뉴 목록과 상세가 URL 하나만 받으면 즉시 이미지를 표시할 수 있다.
- 메뉴 API 응답이 단순하다.

여기서 말하는 `public`은 **프런트 프로젝트의 `public` 폴더**다. 빌드·개발 서버가 그 파일을 정적 에셋으로 제공하므로, 화면은 `<img src={imageUrl}>`로 해당 URL을 바로 사용했다.

백엔드에 있는 `/uploads/menu/**` 정적 리소스 설정은 별도로 추가된 로컬 업로드 지원 코드다. 이것은 처음 프런트 `public` 에셋 URL을 선택한 이유가 아니며, 현재 `media_asset` 생성 흐름과도 아직 직접 연결되어 있지 않다.

## 2. 기존 방식의 한계

`menu.image_url`만 사용하면 이미지 파일 자체의 정보가 메뉴 데이터에 섞인다.

- Cloudinary 등 저장소 제공자 정보가 메뉴 테이블에 없다.
- `public_id`, 포맷, 가로·세로, 용량, 업로드 시각을 관리할 자리가 없다.
- 같은 이미지가 여러 메뉴에서 사용될 때 중복 URL 문자열을 저장한다.
- 이미지 교체·삭제·정리 시 어떤 메뉴가 자산을 참조하는지 FK로 확인할 수 없다.

따라서 이미지 자산의 책임을 `media_asset`으로 분리하고, 메뉴는 그 자산의 ID만 참조하도록 정규화한다.

## 3. 현재 데이터 구조

```text
menu.image_asset_id ────────> media_asset.id
                                  ├─ provider_id
                                  ├─ public_id
                                  ├─ url
                                  ├─ format
                                  ├─ width / height / bytes
                                  ├─ uploaded_at
                                  └─ deleted_at
```

실제 DB에는 이미 아래 FK가 존재한다.

```sql
CONSTRAINT fk_menu_image_asset_id
  FOREIGN KEY (image_asset_id) REFERENCES media_asset (id)
```

`image_url`은 기존 데이터 이관 확인을 위해 당분간 유지한다. 신규 메뉴 이미지의 정본은 `image_asset_id`이며, 조회할 URL은 `media_asset.url`이다.

## 4. 이제 어떻게 사용하나

### 관리자: 메뉴 등록·수정

이미지 업로드 또는 이미지 선택 과정이 먼저 `media_asset`에 자산을 등록한다. 그 결과로 받은 `id`를 메뉴 요청의 `mediaAssetId`로 전달한다.

```json
{
  "categoryId": 1,
  "name": "스파이시 쉬림프 샌드위치",
  "price": 8900,
  "mediaAssetId": 321,
  "description": "메뉴 설명"
}
```

백엔드는 `mediaAssetId`가 실제로 존재하고 `deleted_at IS NULL`인 자산인지 확인한 뒤 `menu.image_asset_id`에 저장한다.

전환 기간 동안 기존 관리자 화면이 `imageUrl`만 보내면, 백엔드는 같은 URL을 가진 활성 `media_asset`을 찾아 연결한다. 해당 자산이 없으면 임의 URL을 `menu`에 저장하지 않고 `MENU_CREATE_INVALID`로 막는다.

> `imageUrl` 호환은 기존 화면을 즉시 깨지 않기 위한 임시 경로다. 관리자 화면은 최종적으로 `mediaAssetId`를 저장해야 한다.

### 고객·관리자: 메뉴 조회

모든 메뉴 조회는 `menu`와 `media_asset`을 `LEFT JOIN`한다.

```sql
LEFT JOIN media_asset ma
  ON ma.id = m.image_asset_id
 AND ma.deleted_at IS NULL
```

응답 계약은 화면 변경을 줄이기 위해 계속 `imageUrl`을 사용한다.

```sql
ma.url AS imageUrl
```

따라서 고객 키오스크와 관리자 화면은 기존처럼 `imageUrl`을 이미지 `src`에 사용하면 된다. 내부 데이터 저장 방식만 URL 직접 저장에서 FK 참조로 변경된 것이다.

## 5. 목록 View와 기존 데이터 전환

관리자 메뉴 목록은 `vw_menu_list`를 사용한다. View도 `m.image_asset_id`와 `media_asset`을 조인해 `ma.url AS image_url`을 반환하도록 변경했다.

기존 `menu.image_url` 데이터는 [2026-08-13_menu_media_asset_normalization.sql](2026-08-13_menu_media_asset_normalization.sql)로 **같은 URL을 가진** `media_asset`이 있을 때만 자동 보정한다.

1. `image_url`과 같은 URL을 가진 활성 `media_asset`을 찾는다.
2. 찾은 자산 ID를 `menu.image_asset_id`에 채운다.
3. 끝까지 연결되지 않은 메뉴 목록을 조회한다.
4. 결과가 0건인지 확인한 뒤 `vw_menu_list`를 재생성한다.

초기 프런트 `public` 에셋 URL(`/assets/menu/...`)은 Cloudinary 등의 `media_asset.url`과 보통 다르므로, 자동 보정 대상에서 남을 가능성이 높다. 이 메뉴들은 이미지 파일을 제공자에 등록해 `media_asset`을 만든 뒤, 해당 메뉴의 `image_asset_id`를 직접 연결해야 한다.

이 SQL은 **DB 컬럼을 새로 만들지 않으며**, 실제 DB에 이미 있는 `image_asset_id`만 채운다. 현재 DB에서 실행한 기록은 아직 없다.

## 6. 주의할 점

- `media_asset.deleted_at`이 설정된 자산은 메뉴 조회에서 이미지 URL을 반환하지 않는다.
- 메뉴가 soft delete되어도 이미지 자산을 자동 삭제하지 않는다. 다른 메뉴가 같은 자산을 참조할 수 있기 때문이다.
- `menu.image_url` 삭제는 기존 데이터 보정, 관리자 화면 전환, 목록·상세 API 검증이 끝난 후 별도 결정한다.
- 현재 메뉴 이미지 업로드 Controller의 실연결 여부와 `media_asset` 생성 책임은 별도 검증 대상이다. 이 문서는 이미 등록된 자산을 메뉴에 연결하고 조회하는 기준만 다룬다.

## 7. 확인 근거

- 실제 DB `menu.image_asset_id` → `media_asset.id` FK 정의 확인
- 관리자 Mapper: 등록·수정·상세에 `image_asset_id` 반영
- 고객 Mapper: 목록·상세에 `media_asset.url AS imageUrl` 반영
- `gradlew.bat compileJava --no-daemon` 성공

## 8. 기준 커밋 이후 변경 이력과 이유

아래 기록은 이미지 운영 방식과 직접 관련된 변경만 정리한 것이다. 커밋이 존재한다는 사실은 실제 DB 반영이나 고객 화면 실연동 완료를 뜻하지 않는다.

### 기준점 A — 고객 키오스크: `4b0cd21b6834e0d1d568c525f1d3d81954259475`

`ASAK-Kiosk`의 2026-08-12 커밋이다. 메뉴 상세 화면을 이전 안정 시점으로 되돌리고, CSS를 제외한 확인 다이얼로그·토스트·옵션 아이템·피드백 아이콘 관련 변경을 제거했다.

이 커밋은 이미지 저장 구조를 바꾼 커밋은 아니다. 이후 public 에셋을 정리하고 외부 자산 전환 준비를 한 변경을 비교할 기준점이다.

| 이후 변경 | 변경 이유 | 범위와 한계 |
| --- | --- | --- |
| `86a1ad0` 메뉴 사진 투명 여백 제거 | 메뉴 카드에서 실제 음식이 너무 작게 보이거나 카드마다 비율이 달라 보이는 문제를 줄이기 위해, 투명 배경은 보존하고 PNG의 불필요한 바깥 여백만 잘랐다. | `public/assets/menu/*.png`의 표시 품질 보정이며, API·DB 구조는 변경하지 않았다. |
| `23cba37` 재료 아이콘 Iconify 교체 | 여러 재료가 범용 아이콘을 공유해 이름과 그림이 맞지 않는 문제를 줄이기 위해, 재료 성격에 맞는 SVG로 재배치했다. | `public/assets/ingredients/icons/*.svg`만 교체했다. 메뉴 이미지 저장 방식과는 별도다. |
| `47f5d90` 샐러디 메뉴·재료 이미지 동기화 | 여러 PC의 프런트 저장소에서 같은 메뉴·재료를 보이게 하고, 중복되거나 잘못 연결된 public 에셋을 정리하기 위해 동기화했다. | `public/assets/**`와 재료 catalog를 정리했다. 이 시점에도 이미지는 프런트 저장소에 포함되어 있다. |
| `80bc998`, `cd88db3` Cloudinary SDK 추가·병합 | 프런트가 외부 이미지 제공자와 통신할 수 있도록 의존성을 준비했다. | SDK 의존성 추가만 확인됐다. 자산 업로드, `media_asset` 생성, 메뉴 FK 저장이 실제로 끝났다는 근거는 아니다. |
| 현재 미커밋 `src/api/client.js` | API endpoint 상수가 이미 `/api/kiosk/...` 전체 경로를 만들기 때문에, Axios 기본 `baseURL`에서 `/api/kiosk`를 다시 붙이면 경로가 중복된다. 기본값을 빈 문자열로 바꿔 Vite `/api` proxy를 사용하도록 했다. | `/api/kiosk/api/kiosk/categories` 요청을 방지하기 위한 API 경로 수정이며, 이미지 자산 처리와는 직접 관계없다. |

### 기준점 B — 메뉴 백엔드: `2b97a5e9d50089a17e754d1db210c3c154c73386`

`ASAK-backend`의 2026-08-12 커밋이다.

- `app.file.menu-upload-dir` 끝에 `/`를 추가해 로컬 업로드 경로 조합을 명확히 했다.
- 고객 메뉴 상세 옵션 조회에서 `ing_nutr` 조인을 제거하고 이미 조인한 `ing.kcal`을 사용했다. 중복·불필요한 영양 조인으로 인한 조회 오류 가능성을 줄이기 위한 수정이다.

이 기준점 이후 이미지 관련 변경은 다음과 같다.

| 이후 변경 | 변경 이유 | 범위와 한계 |
| --- | --- | --- |
| `1add7f1` Cloudinary 환경변수 예시 | Cloudinary cloud name·API key·secret을 코드에 넣지 않고 로컬 환경에서 주입할 수 있도록 `.env.example`에 변수 형식을 추가했다. | 환경변수 예시만 추가했다. 업로드 API와 `media_asset` INSERT가 구현·검증됐다는 뜻은 아니다. |
| 현재 미커밋 관리자 DTO·Service·Mapper | 메뉴가 URL 문자열을 직접 저장하는 대신 실제 DB FK `menu.image_asset_id`를 저장하도록 전환했다. `mediaAssetId`가 활성 `media_asset`인지 검증하고 저장한다. | 기존 관리자 화면의 `imageUrl`도 전환 기간에는 활성 자산 URL 조회로 호환한다. 매칭되는 자산이 없으면 임의 URL 저장 대신 요청을 거절한다. |
| 현재 미커밋 고객 Mapper·관리자 목록 View | 고객 메뉴 목록·상세와 관리자 메뉴 상세·목록이 `media_asset.url`을 조인해 기존 응답 필드 `imageUrl`로 반환하도록 변경했다. | 화면은 `imageUrl`을 계속 사용하므로 즉시 props 변경은 필요 없다. `image_asset_id`가 비어 있거나 자산이 soft delete면 이미지 URL은 null이다. |
| 현재 미커밋 데이터 보정 SQL | 기존 `menu.image_url`과 같은 활성 `media_asset.url`을 가진 행만 찾아 `image_asset_id`를 채우도록 준비했다. | 프런트 public URL과 제공자 URL은 보통 달라 자동 연결되지 않는다. 실제 DB 실행 기록은 아직 없고, 미연결 메뉴는 수동 연결 대상이다. |

### 최종 데이터 흐름

```text
과거: 프런트 public 이미지 URL → menu.image_url → API imageUrl → 화면 img src

전환: 이미지 제공자 업로드/선택 → media_asset 생성 → menu.image_asset_id
      → media_asset.url 조인 → API imageUrl → 화면 img src
```

화면이 받는 `imageUrl`은 유지한다. 바뀐 것은 그 URL의 저장 책임이 `menu`에서 `media_asset`으로 옮겨간 점이다.
