# ASAK 문서 동기화 — PDF 대조 정리 운영 결정 (2026-08-19)

- 대상 문서: `docs/pdf-intersection-cleanup-plan-2026-08-19.md`
- 범위: PDF 대조 결과를 실제 삭제 지시로 오해하지 않도록 운영 처리 기준 기록
- 소스코드·실제 DB·Git: 수정하지 않음

## 확인 근거

- `docs/아삭_mysql.sql`
  - `menu.deleted_at` 존재
  - `opt_policy_item.active` 존재
- `src/main/resources/mappers/UserMenuMapper.xml`
  - 메뉴 목록·상세는 `m.deleted_at IS NULL`로 soft-deleted 메뉴를 제외
  - 옵션 조회는 현재 `opi.active`를 필터링하지 않음
- `src/main/resources/mappers/UserOrderMapper.xml`
  - 옵션 주문 검증은 현재 `opi.active`를 필터링하지 않음

## 기록한 결정

- 깨진 「빼기」 옵션 4개와 드레싱·토핑은 `opt_policy_item.active=0`으로 비노출 처리하되, Mapper의 `opi.active=1` 반영 후 적용한다.
- 메뉴 15개는 `menu.deleted_at` soft delete로 비노출 처리한다.
- 생수 330ml과 정상 재료 빼기 기능은 유지한다.
- PDF 미수록은 단종 또는 삭제 근거가 아니다.

## 반영 전 남은 작업

- `UserMenuMapper.xml`, `UserOrderMapper.xml` 코드 수정 승인
- 대상 `opt_policy_item` 행과 메뉴 15개 ID를 실제 DB에서 재조회
- 백업, DB 갱신, 키오스크 메뉴·옵션 조회와 주문 검증 회귀 확인
