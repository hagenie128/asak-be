# GitHub Issues 가이드 (ASAK)

> **2026-07-16 override:** issue, branch, and PR instructions must distinguish the current four repositories. Do not create Kiosk migration issues that change remotes, pull, reset, rebase, or move files until the canonical repository decision is approved. WBS execution IDs use `WBS2-*`; legacy WBS identifiers remain historical records.

> **Notion:** [01. 팀/역할/일정](https://app.notion.com/p/15451ef04f0b821c83568124e6ebb32f) · [📖 문서 읽는 순서](https://app.notion.com/p/39451ef04f0b81088a91d914f985fb11)  
> **Git:** [`docs/guides/README.md`](README.md) — 가이드 읽기 순서 **02**  
> **Git (원본):** [docs/guides/02-github-issues-guide.md](https://github.com/hagenie128/ASAK/tree/main/docs/guides/02-github-issues-guide.md) — ASAK repo

통합 저장소 [`hagenie128/ASAK`](https://github.com/hagenie128/ASAK)에서 WBS·요구사항·워크로그를 이슈로 연결하는 방법입니다.

## 이슈 템플릿

| 템플릿 | 파일 | 용도 |
|--------|------|------|
| 작업 / 기능 | `.github/ISSUE_TEMPLATE/task.yml` | WBS 단위 기능·작업 |
| 버그 | `.github/ISSUE_TEMPLATE/bug.yml` | 재현 가능한 버그 |

`config.yml`로 빈 이슈(blank issue)는 비활성화되어 있습니다. **New issue** 시 템플릿을 선택하세요.

## 라벨 목록

| Label | Color | 용도 |
|-------|-------|------|
| `wbs` | `#0E8A16` | WBS 연동 작업 |
| `frontend` | `#1D76DB` | ASAK-front |
| `backend` | `#5319E7` | ASAK-back |
| `bug` | `#D73A4A` | 버그 |
| `feature` | `#A2EEEF` | 기능 |
| `blocked` | `#B60205` | 블로커 |
| `data` | `#FBCA04` | 시드/크롤링/DB |
| `docs` | `#0075CA` | 문서 |
| `week-1` ~ `week-9` | `#6A737D` | 9주 마일스톤 (7/2~9/2) |

### 라벨 조합 예시

- `WBS-010` 메뉴 옵션 UI → `wbs`, `frontend`, `feature`, `week-2`
- 시드 JSON 수정 → `data`, `ASAK(통합)` 작업 시 `docs` 또는 `data`
- 결제 API 버그 → `bug`, `backend`, `blocked`(외부 의존 시)

## WBS / 요구사항 / 워크로그 연동

```text
WBS (Notion·DevCopilot)  →  GitHub Issue (#번호)  →  worklog/entries/{이름}/ 상세 기록
```

1. **Issue 생성**: `task.yml`에 WBS ID·요구사항 ID·완료 조건·저장소를 작성
2. **라벨**: 저장소(`frontend`/`backend`), 유형(`feature`/`bug`), 해당 주차(`week-N`) 추가
3. **워크로그**: 구현·디버깅 상세는 `worklog/entries/{이름}/` (12섹션)에 작성하고 이슈 본문에 경로 링크
4. **일일 요약**: `worklog/daily/{이름}/YYYY-MM-DD.md` — **오늘 요약** 표에 이슈·상태, **오늘 작업** 미니 카드에 `entries/{이름}/` 링크

자세한 워크로그 구조는 [`worklog/README.md`](../../worklog/README.md)를 참고하세요.

## 라벨 생성 (gh CLI)

저장소 루트에서 한 번 실행합니다.

```powershell
cd C:\ASAK   # ASAK 통합 저장소 로컬 경로

gh label create wbs --color 0E8A16 --description "WBS 연동 작업" --repo hagenie128/ASAK
gh label create frontend --color 1D76DB --description "ASAK-front" --repo hagenie128/ASAK
gh label create backend --color 5319E7 --description "ASAK-back" --repo hagenie128/ASAK
gh label create bug --color D73A4A --description "버그" --repo hagenie128/ASAK
gh label create feature --color A2EEEF --description "기능" --repo hagenie128/ASAK
gh label create blocked --color B60205 --description "블로커" --repo hagenie128/ASAK
gh label create data --color FBCA04 --description "시드/크롤링/DB" --repo hagenie128/ASAK
gh label create docs --color 0075CA --description "문서" --repo hagenie128/ASAK

1..9 | ForEach-Object {
  gh label create "week-$_" --color 6A737D --description "9주 마일스톤 $_주차" --repo hagenie128/ASAK
}
```

이미 있는 라벨은 `gh label create ... --force`로 색·설명만 갱신할 수 있습니다.

### 수동 생성 (gh 실패 시)

1. GitHub → **hagenie128/ASAK** → **Issues** → **Labels** → **New label**
2. 위 표의 이름·색(hex `#` 제외)·설명을 그대로 입력
3. `week-1` … `week-9`까지 9개 추가

`gh` 인증: `gh auth login` 후 `gh auth status`로 확인합니다.

## 프론트·백엔드 저장소

이슈·라벨 규칙은 **ASAK 통합 저장소** 기준입니다. `ASAK-front`, `ASAK-back`에서도 동일 라벨을 쓰려면 각 저장소에 위 명령을 `--repo hagenie128/ASAK-front` 형태로 반복 실행하세요.

## 관련 문서

- [`01-team-setup.md`](01-team-setup.md) — 저장소·브랜치·기록 규칙
- [`worklog/README.md`](../../worklog/README.md) — 일일·entries 워크로그
- [`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md) — PR 본문 템플릿
