# 스키마 문서 동기화 도구

`schema_sync.py` 는 운영 DB(`asak_db`)의 실제 DDL 을 읽어, 스키마 문서 두 개가
실제와 맞는지 대조하고 어긋나면 갱신한다.

| 문서 | 내용 | 도구가 갱신 |
|---|---|---|
| `docs/아삭_mysql.sql` | 테이블 26개 DDL + 외래키 46개 | O (`sync --write`) |
| `docs/view.sql` | 뷰 22개 정의 | X (대조·검증만) |

## 왜 필요한가

2026-08-19 에 `docs/아삭_mysql.sql` 이 실제 DB 와 크게 어긋나 있는 것이 발견됐다.
`media_asset` 테이블이 통째로 빠져 있었고, `payment.idempotency_key` 가 없어서
문서만 보고 INSERT 하면 `Field 'idempotency_key' doesn't have a default value` 로
실패했다. AUTO_INCREMENT, DEFAULT, UNIQUE 도 대부분 누락돼 있었고 외래키는 46개
전부 이름이 달랐다. 자세한 내역은 `docs/schema-doc-drift-2026-08-19.md` 에 있다.

손으로 맞추면 또 어긋난다. 그래서 실제 DB 에서 뽑아 생성하도록 도구로 만들었다.

## 준비

```bash
pip install pymysql
```

저장소 루트의 `.env` 에서 접속 정보를 읽는다 (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

## 사용법

저장소 루트에서 실행한다.

```bash
python docs/tools/schema_sync.py all
```

`all` 은 `dump` → `diff` → `verify` 를 차례로 돈다. 문서를 실제로 고치는 `sync` 는
일부러 빼놨다.

개별 명령:

| 명령 | 하는 일 |
|---|---|
| `dump` | 실제 DDL 을 `out/` 에 덤프 (다른 명령의 입력) |
| `diff` | 문서 vs 실제 대조표를 `out/drift-report.md` 로 생성 |
| `sync` | `아삭_mysql.sql` 재생성. 기본은 `out/` 에 미리보기만 |
| `sync --write` | 재생성 결과를 문서에 반영 |
| `verify` | 문서와 실제가 같은지 검증 |

문서를 실제 기준으로 갱신하는 전형적인 흐름:

```bash
python docs/tools/schema_sync.py dump
python docs/tools/schema_sync.py diff
python docs/tools/schema_sync.py sync --write
python docs/tools/schema_sync.py verify
```

Windows 콘솔에서 한글이 깨지면 앞에 `PYTHONIOENCODING=utf-8` 을 붙인다.

## 읽기 전용이다

DB 는 `nam3324.synology.me:33338/asak_db` 로 **팀 공용**이다. 이 도구는 스키마를
바꾸지 않는다. `SELECT` / `SHOW` / `DESCRIBE` / `EXPLAIN` 외의 SQL 은 실행 전에
막고 예외를 던진다 (`ReadOnlyDB.ALLOWED`). 커넥션은 항상 `rollback()` 후 닫는다.

## 검증 방식

**테이블** — 문서의 `CREATE TABLE` 본문과 `ALTER TABLE ... ADD CONSTRAINT` 를 다시
합쳐 `SHOW CREATE TABLE` 형태로 되돌린 뒤, 컬럼 / 키·인덱스 / 외래키 세 덩어리로
나눠 비교한다. 컬럼은 순서까지 본다.

**뷰** — `SHOW CREATE VIEW` 본문을 문자열 리터럴은 보존한 채 키워드만 소문자화하고
공백을 정규화해 토큰 단위로 비교한다.

토큰이 다르다고 의미까지 다른 것은 아니다. MySQL 은 조인 트리에 중첩 괄호를 붙여 저장하는데
`view.sql` 은 가독성을 위해 그 괄호를 뺐다. 그래서 차이를 두 종류로 나눈다.

- **괄호 표기 차이** — 차이나는 토큰이 전부 `(` 또는 `)` 인 경우. 의미가 같다.
  이때만 **실제 뷰와 문서 SQL 을 각각 실행해** 행 수, 전체 행 체크섬
  (`BIT_XOR(CRC32)` + `SUM(CRC32)`), 컬럼 이름·순서를 비교해 확인한다.
- **정의 차이** — 리터럴이나 식별자가 다른 경우. 즉시 불일치로 판정하고 종료코드 1 을 낸다.

정의 차이를 실행 비교로 통과시키지 않는 이유가 있다. 실행 비교는 "지금 데이터에서 결과가 같다"만
보이지 "정의가 같다"를 보이지 못한다. 실제로 2026-08-19 에 `vw_order_live` 의 상태 필터가
문서에는 `IN ('RECEIVED','PREPARING','READY')`, 실제 DB 에는 `IN ('RECEIVED','PREPARING')` 으로
달랐는데 두 SQL 의 실행 결과가 같아서 초기 버전이 이를 "표기 차이"로 잘못 통과시켰다.

## 도구가 일부러 빼는 것

- **`AUTO_INCREMENT=nnn` 현재값** — 런타임 값이라 기록하는 순간 낡는다.
- **컬럼 단위 `COLLATE`** — 테이블 기본값(`utf8mb4_unicode_ci`)과 같아 중복이다.
- **`backup_*` / `*_backup_*` 테이블** — 일회성 백업본이라 스키마 문서 대상이 아니다.
  2026-08-19 기준 22개 있다.

## 알아둘 것

`아삭_mysql.sql` 의 헤더는 `sync --write` 가 통째로 다시 쓴다. 헤더에 손으로 메모를
남겨도 다음 실행 때 사라진다. 남길 내용이 있으면 별도 문서에 쓸 것.

실제 뷰는 전부 ``DEFINER=`asakasak`@`%` ``, `SQL SECURITY DEFINER` 로 만들어져 있다.
`view.sql` 의 `CREATE OR REPLACE VIEW` 에는 DEFINER 절이 없으므로, 그대로 적용하면
실행한 계정이 definer 가 된다. 운영에 적용할 때는 권한 계정으로 실행하거나 DEFINER 를
명시해야 한다.
