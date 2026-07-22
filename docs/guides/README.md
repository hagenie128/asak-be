# ASAK 팀 가이드 (읽기 순서)

> 태그: `#reference`  
> 제품·API 정본은 [Product Bible](../product_bible/) / [Canonical Contract](../governance/canonical-contract-decisions-2026-07-16.md).  
> 전체 지도: [document-tag-index-2026-07-20.md](../document-tag-index-2026-07-20.md)

> **00. 온보딩:** Notion [🚀 ASAK 처음 시작하기](https://app.notion.com/p/39551ef04f0b8193ae2ad4d529ab2d7b) · Git [`getting-started.md`](../operations/setup/getting-started.md)  
> **Notion:** [📖 문서 읽는 순서](https://app.notion.com/p/39451ef04f0b81088a91d914f985fb11) · [📚 팀 문서 안내](https://app.notion.com/p/39551ef04f0b813b8765e64384f2dfd3)  
> **Git:** [`docs/guides/`](https://github.com/hagenie128/ASAK/tree/main/docs/guides) — ASAK repo

**Step 0 (Windows)** — PC에 Git·Python·Java·Node가 **아직 없으면** [`install-windows.md`](../operations/setup/install-windows.md) (클릭·폴더·PATH 상세) → [`getting-started.md`](../operations/setup/getting-started.md) (세팅 + 워크로그) → 아래 **01~06**.

| 순서 | 문서 | 내용 |
|------|------|------|
| 00a | [`install-windows.md`](../operations/setup/install-windows.md) | **Windows 설치** (자동 `scripts/setup-windows.ps1` + 수동 fallback) |
| 00b | [`mcp-setup.md`](../operations/setup/mcp-setup.md) | **Cursor MCP** (Notion·환경 변수) |
| 00 | [`getting-started.md`](../operations/setup/getting-started.md) | **완전 초보 온보딩** (세팅 + 워크로그) |
| 01 | [`01-team-setup.md`](01-team-setup.md) | 저장소 클론·세팅·Git·9주 일정·온보딩 체크리스트 |
| 02 | [`02-github-issues-guide.md`](02-github-issues-guide.md) | GitHub Issue·라벨·WBS·워크로그 연동 |
| 03 | [`03-work-log-template.md`](03-work-log-template.md) | 기능·이슈 단위 작업 기록 (12섹션) · [`일일 워크로그와의 관계`](03-work-log-template.md#일일-워크로그와의-관계) |
| 04 | [`04-sample-work-log-example.md`](04-sample-work-log-example.md) | 상세 기록 예시 (SCR-003 · 이하진) · daily 연결은 [`worklog/guide-team-daily.md`](../../worklog/guide-team-daily.md) |
| 05 | [`05-personal-portfolio-template.md`](05-personal-portfolio-template.md) | 프로젝트 종료 후 포트폴리오 정리 |
| 06 | [`06-team-ai-prompt.md`](06-team-ai-prompt.md) | 팀 공통 AI 프롬프트 (Cursor·ChatGPT 등) |

### 구현 가이드 (07~11은 여기로 통합)

프론트/백/흐름 문서는 **`implementation_guide`가 정본**입니다.

| 예전 guides | 지금 |
|---|---|
| 07 프론트 | [stub](07-frontend-development-guide.md) → [00_START_HERE](../implementation_guide/00-start-here.md) |
| 08 백엔드 | [stub](08-backend-development-guide.md) → [04 API·DB](../implementation_guide/04-api-db-implementation.md) |
| 09~11 흐름·순서 | [09-11-moved](09-11-moved.md) |

**일일 워크로그**는 별도 흐름입니다 → [`worklog/README.md`](../../worklog/README.md) 확인 순서 · Notion [📅 일일 워크로그 — 팀 가이드](https://app.notion.com/p/39451ef04f0b81c0a018e8fe6ea9fb95) · [📅 일일 워크로그 DB](https://app.notion.com/p/eeae4beb07ad4051928a87de0ea4c8f9)
