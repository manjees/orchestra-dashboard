# PRD: Orchestra Dashboard — AI Orchestrator Monitoring & Control

> **Version:** 1.0
> **Date:** 2026-03-28
> **Status:** Draft

---

## 1. Problem Statement

ai-orchestrator는 텔레그램 봇 기반의 AI 개발 자동화 도구로, GitHub 이슈 자동 해결(solve), 프로젝트 초기화(init), 기술 상담(discuss) 등 강력한 파이프라인을 제공한다.

현재 모든 상호작용은 텔레그램 채팅으로만 이뤄지며, 다음과 같은 한계가 있다:

- **파이프라인 가시성 부족** — 9단계 파이프라인이 어디까지 진행됐는지 텔레그램 메시지 편집으로만 확인 가능. 동시 실행(parallel) 시 메시지가 뒤섞여 추적이 어려움
- **히스토리 부재** — 과거 파이프라인 실행 결과, 성공/실패 패턴, 소요 시간 추이를 볼 수 없음
- **시스템 모니터링 단편적** — `/status`로 순간 스냅샷만 확인 가능. 시간에 따른 리소스 추이 파악 불가
- **명령 실행 불편** — 텔레그램에서 `/solve project #1 #2 --parallel --full` 같은 복잡한 커맨드를 타이핑해야 함
- **오픈소스 접근성** — 텔레그램 봇 설정 없이도 orchestrator를 사용하고 싶은 개발자 존재

Orchestra Dashboard는 이 문제들을 해결하는 **웹/데스크톱/모바일 대시보드**로, orchestrator의 실시간 상태를 시각화하고 직접 명령을 내릴 수 있는 통합 인터페이스를 제공한다.

---

## 2. Target Users

| 페르소나 | 설명 |
|----------|------|
| **Solo Developer** | ai-orchestrator를 로컬에서 돌리며 개인 프로젝트를 자동화하는 개발자. 텔레그램 대신 대시보드에서 모니터링/제어하고 싶음 |
| **Open Source Contributor** | orchestrator를 fork해서 쓰는 외부 개발자. 설치가 쉽고 별도 설정 없이 바로 쓸 수 있어야 함 |

---

## 3. Architecture

### 3.1 Integration Model

ai-orchestrator(Python)에 **FastAPI REST/WebSocket 레이어**를 추가하고, orchestra-dashboard가 이를 소비한다.

```
┌─────────────────────────────────────────────────────────────┐
│                    ai-orchestrator (Python)                  │
│                                                             │
│  ┌──────────────┐   ┌──────────────────────────────────┐    │
│  │ Telegram Bot  │   │ FastAPI Server (:9000)  ← NEW    │    │
│  │ (기존 유지)    │   │                                  │    │
│  │              │   │  REST  — 조회/명령                 │    │
│  │              │   │  WebSocket — 실시간 이벤트 스트림    │    │
│  └──────┬───────┘   └──────────────┬───────────────────┘    │
│         │                          │                         │
│         └──────────┬───────────────┘                         │
│                    ▼                                         │
│         Shared Core (pipeline, scheduler, config)            │
└─────────────────────────────────────────────────────────────┘
                          │
                   HTTP / WebSocket
                          │
┌─────────────────────────┴───────────────────────────────────┐
│                  orchestra-dashboard                         │
│                                                             │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐     │
│  │ Spring Boot │  │ Desktop    │  │ Android / iOS      │     │
│  │ Server     │◀─│ (Compose)  │  │ (Compose / SwiftUI)│     │
│  │ (BFF+Cache)│  └────────────┘  └────────────────────┘     │
│  └────────────┘                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Why FastAPI in Orchestrator

| 대안 | 문제 |
|------|------|
| Dashboard가 파일 직접 읽기 | orchestrator 내부 구조에 강결합. 파일 포맷 바뀌면 dashboard도 깨짐 |
| Dashboard에 orchestrator 로직 복제 | 두 곳에서 같은 로직 관리. 동기화 불가능 |
| **FastAPI를 orchestrator에 추가** | orchestrator가 자기 데이터의 주인. 깔끔한 API 계약. 텔레그램 봇과 동일 async loop에서 실행 가능 |

### 3.3 Data Flow

```
[사용자가 Dashboard에서 "Solve #5" 클릭]
    → Dashboard App → Spring Boot Server
    → POST orchestrator:9000/api/commands/solve
    → orchestrator가 파이프라인 시작
    → WebSocket으로 실시간 스텝 진행 push
    → Dashboard가 실시간 UI 업데이트
```

---

## 4. Orchestrator API Specification (FastAPI)

orchestrator에 추가할 API. 기존 내부 데이터 구조를 그대로 노출한다.

### 4.1 System

| Method | Path | Description | Source |
|--------|------|-------------|--------|
| `GET` | `/api/status` | 시스템 상태 (RAM, CPU, Disk, Thermal) | `system_monitor.get_system_status()` |
| `GET` | `/api/status/ollama` | Ollama 모델 목록 및 상태 | `ollama_provider.list_models()` |
| `GET` | `/api/status/tmux` | 활성 tmux 세션 목록 | `tmux_manager.list_sessions()` |

**Response: `GET /api/status`**
```json
{
  "ram_total_gb": 32.0,
  "ram_used_gb": 18.5,
  "ram_percent": 57.8,
  "cpu_percent": 42.3,
  "thermal_pressure": "nominal",
  "disk_total_gb": 1000.0,
  "disk_used_gb": 450.2,
  "disk_percent": 45.0,
  "ollama": {
    "online": true,
    "models": [
      {"name": "deepseek-r1:32b", "size_gb": 18.5},
      {"name": "qwen3.5:35b", "size_gb": 20.1}
    ]
  },
  "tmux_sessions": [
    {"name": "ai_factory", "windows": 3, "created": "2026-03-28T10:00:00"}
  ]
}
```

### 4.2 Projects

| Method | Path | Description | Source |
|--------|------|-------------|--------|
| `GET` | `/api/projects` | 등록된 프로젝트 목록 | `config.load_projects()` |
| `GET` | `/api/projects/{name}` | 프로젝트 상세 (최근 이슈, summary) | `projects.json` + `project_summary.json` |
| `GET` | `/api/projects/{name}/issues` | 프로젝트의 open GitHub 이슈 | `gh issue list` |

**Response: `GET /api/projects`**
```json
[
  {
    "name": "orchestra-dashboard",
    "path": "~/Desktop/dev/orchestra-dashboard",
    "ci_commands": ["./gradlew build", "./gradlew test"],
    "open_issues_count": 3,
    "recent_solves": 12
  }
]
```

**Response: `GET /api/projects/{name}/issues`**
```json
[
  {
    "number": 30,
    "title": "feat: Implement agent control commands via WebSocket and REST",
    "labels": ["feature", "websocket"],
    "state": "open",
    "created_at": "2026-03-11T01:02:59Z"
  }
]
```

### 4.3 Pipelines

| Method | Path | Description | Source |
|--------|------|-------------|--------|
| `GET` | `/api/pipelines` | 활성 + 최근 완료된 파이프라인 목록 | 런타임 상태 |
| `GET` | `/api/pipelines/{id}` | 파이프라인 상세 (PipelineContext) | 런타임 상태 / checkpoint |
| `GET` | `/api/checkpoints` | 저장된 체크포인트 목록 | `checkpoint.list_checkpoints()` |

**Response: `GET /api/pipelines`**
```json
[
  {
    "id": "orchestra-dashboard_30",
    "project_name": "orchestra-dashboard",
    "issue_num": 30,
    "issue_title": "feat: Implement agent control commands",
    "mode": "standard",
    "status": "running",
    "current_step": "Sonnet Implement",
    "started_at": "2026-03-28T14:00:00Z",
    "steps": [
      {"name": "Haiku Research", "status": "passed", "elapsed_sec": 45.2},
      {"name": "Opus Design", "status": "passed", "elapsed_sec": 320.8},
      {"name": "Gemini Design Critique", "status": "passed", "elapsed_sec": 180.5},
      {"name": "Qwen Hints", "status": "passed", "elapsed_sec": 90.1},
      {"name": "Sonnet Implement", "status": "running", "elapsed_sec": 0},
      {"name": "Local CI Check", "status": "pending", "elapsed_sec": 0},
      {"name": "Sonnet Self-Review", "status": "pending", "elapsed_sec": 0},
      {"name": "Gemini Cross-Review", "status": "pending", "elapsed_sec": 0},
      {"name": "AI Audit", "status": "pending", "elapsed_sec": 0}
    ],
    "elapsed_total_sec": 636.6
  }
]
```

### 4.4 Commands (Direct Control)

| Method | Path | Description | Equivalent Telegram Command |
|--------|------|-------------|----------------------------|
| `POST` | `/api/commands/solve` | 이슈 자동 해결 시작 | `/solve` |
| `POST` | `/api/commands/retry` | 실패한 파이프라인 재시도 | `/retry` |
| `POST` | `/api/commands/cancel` | 실행 중인 파이프라인 취소 | Telegram 인라인 버튼 |
| `POST` | `/api/commands/init` | 새 프로젝트 초기화 | `/init` |
| `POST` | `/api/commands/plan` | 프로젝트 이슈 기획 | `/plan` |
| `POST` | `/api/commands/discuss` | 기술 상담 | `/discuss` |
| `POST` | `/api/commands/rebase` | PR 리베이스 | `/rebase` |
| `POST` | `/api/commands/shell` | 셸 명령 실행 | `/cmd` |

**Request: `POST /api/commands/solve`**
```json
{
  "project": "orchestra-dashboard",
  "issues": [30, 31],
  "mode": "standard",
  "parallel": true
}
```

**Response:**
```json
{
  "command_id": "cmd_abc123",
  "status": "accepted",
  "pipelines": ["orchestra-dashboard_30", "orchestra-dashboard_31"]
}
```

**Request: `POST /api/commands/cancel`**
```json
{
  "pipeline_id": "orchestra-dashboard_30"
}
```

### 4.5 WebSocket — Real-time Events

| Endpoint | Description |
|----------|-------------|
| `WS /ws/events` | 모든 파이프라인/시스템 이벤트 스트림 |

**Event Types:**
```json
{"type": "pipeline.started", "pipeline_id": "proj_30", "mode": "standard", "timestamp": "..."}
{"type": "step.started", "pipeline_id": "proj_30", "step": "Opus Design", "timestamp": "..."}
{"type": "step.completed", "pipeline_id": "proj_30", "step": "Opus Design", "status": "passed", "elapsed_sec": 320.8}
{"type": "step.failed", "pipeline_id": "proj_30", "step": "AI Audit", "detail": "Critical finding: ..."}
{"type": "pipeline.completed", "pipeline_id": "proj_30", "status": "success", "pr_url": "https://..."}
{"type": "pipeline.failed", "pipeline_id": "proj_30", "status": "failed", "failed_step": "Local CI Check"}
{"type": "approval.required", "pipeline_id": "proj_30", "approval_type": "strategy", "context": {...}}
{"type": "supreme_court.required", "pipeline_id": "proj_30", "ruling": "...", "options": ["uphold", "overturn"]}
{"type": "system.status", "ram_percent": 57.8, "cpu_percent": 42.3, "thermal": "nominal"}
```

### 4.6 Approvals (Interactive)

파이프라인 중 사용자 승인이 필요한 단계:

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/approvals/{id}/respond` | Strategy 승인/거부, Supreme Court 판결 응답 |

**Request:**
```json
{
  "decision": "approve",
  "comment": ""
}
```

---

## 5. Dashboard Features

### 5.1 Dashboard Home (실시간 개요)

| 요소 | 설명 |
|------|------|
| **System Health Bar** | RAM, CPU, Disk, Thermal 게이지. 1초 간격 업데이트 |
| **Active Pipelines** | 현재 실행 중인 파이프라인 카드. 실시간 스텝 진행 표시 |
| **Recent Results** | 최근 10개 파이프라인 결과 (성공/실패/소요시간) |
| **Quick Actions** | "New Solve", "View Projects" 버튼 |

### 5.2 Pipeline Monitor (핵심 화면)

| 요소 | 설명 |
|------|------|
| **Step Timeline** | 9단계 파이프라인을 수평 타임라인으로 시각화. 각 스텝의 상태(pending/running/passed/failed)와 소요시간 표시 |
| **Parallel View** | `--parallel` 실행 시 여러 파이프라인을 병렬 레인으로 표시. 의존 관계 화살표 |
| **Live Log** | 현재 실행 중인 스텝의 출력 스트리밍 |
| **Approval Dialog** | Strategy 승인 요청, Supreme Court 판결이 필요할 때 모달 표시 |
| **Cancel Button** | 실행 중인 파이프라인 취소 |

### 5.3 Project Explorer

| 요소 | 설명 |
|------|------|
| **Project List** | 등록된 프로젝트 카드 (이름, 경로, open 이슈 수) |
| **Issue List** | 프로젝트 선택 시 open 이슈 표시. 각 이슈에 "Solve" 버튼 |
| **Solve Dialog** | 이슈 선택, 모드(express/standard/full) 선택, parallel 옵션 → 실행 |
| **Checkpoint List** | 실패한 파이프라인 체크포인트. "Retry" 버튼 |

### 5.4 Command Center

| 요소 | 설명 |
|------|------|
| **Init Project** | 프로젝트명, 설명, public/private 입력 → `/init` 실행 |
| **Plan Issues** | 프로젝트 선택 → Opus가 이슈 기획 |
| **Discuss** | 프로젝트 + 질문 입력 → 기술 상담 결과 표시 |
| **Shell** | 셸 커맨드 입력/실행 (보안 경고 표시) |

### 5.5 History & Analytics (Phase 2)

| 요소 | 설명 |
|------|------|
| **Pipeline History** | 전체 파이프라인 실행 이력. 필터(프로젝트, 상태, 기간) |
| **Success Rate** | 프로젝트별/모드별 성공률 차트 |
| **Duration Trends** | 평균 소요시간 추이 |
| **Step Failure Heatmap** | 어떤 스텝에서 가장 많이 실패하는지 |

---

## 6. Implementation Phases

### Phase 1: API Layer + Read-Only Dashboard

**Orchestrator 측 (ai-orchestrator)**
1. FastAPI 서버 추가 — 기존 텔레그램 봇과 같은 async loop에서 실행
2. `GET` 엔드포인트 구현 — status, projects, pipelines, checkpoints
3. WebSocket `/ws/events` — 파이프라인 이벤트 브로드캐스트
4. 파이프라인 실행 시 이벤트 emit 로직 추가 (기존 `progress_cb` 확장)

**Dashboard 측 (orchestra-dashboard)**
5. Spring Boot 서버 — orchestrator API 호출 프록시/캐싱
6. Dashboard Home 화면 — 시스템 상태 + 활성 파이프라인
7. Pipeline Monitor 화면 — 실시간 스텝 타임라인
8. Project Explorer 화면 — 프로젝트/이슈 목록

### Phase 2: Command & Control

**Orchestrator 측**
9. `POST /api/commands/*` 엔드포인트 구현
10. `POST /api/approvals/*/respond` 구현
11. 인증 (API Key) 추가

**Dashboard 측**
12. Solve Dialog — 이슈 선택 + 모드 선택 → 실행
13. Approval Modal — Strategy/Supreme Court 승인 UI
14. Init/Plan/Discuss 화면
15. Shell 명령 실행 화면

### Phase 3: Analytics & Polish

16. 파이프라인 실행 이력 DB 저장 (orchestrator or dashboard)
17. History 화면 — 필터, 검색
18. Analytics 차트 — 성공률, 소요시간, 실패 분포
19. iOS/Android 모바일 앱 연동
20. 알림 시스템 — 파이프라인 완료/실패 시 push 알림

---

## 7. Technical Decisions

| 결정 | 근거 |
|------|------|
| **FastAPI를 orchestrator에 추가** | uvicorn과 telegram bot이 같은 asyncio loop 공유 가능. 별도 프로세스 불필요 |
| **Spring Boot를 BFF로 유지** | 히스토리 저장, 캐싱, 인증 처리. KMP 클라이언트가 직접 orchestrator를 호출하면 설정이 복잡해짐 |
| **WebSocket for 실시간** | 파이프라인 스텝이 수십 초~수분 단위로 진행. Polling은 비효율적 |
| **orchestrator가 데이터의 주인** | Dashboard는 뷰어+리모컨. 핵심 로직과 상태는 orchestrator에만 존재 |
| **API Key 인증** | 로컬 네트워크 사용 기본. 외부 노출 시 API Key 필수 |

---

## 8. Security Considerations

| 항목 | 대책 |
|------|------|
| **Shell 명령 실행** | Dashboard에서 `/cmd` 실행 시 확인 모달 필수. 위험 명령(rm, kill) 경고 |
| **API 인증** | `DASHBOARD_API_KEY` 환경 변수. 미설정 시 localhost만 허용 |
| **Secret Masking** | orchestrator의 기존 `security.mask_secrets()` 적용. API 응답에서도 시크릿 마스킹 |
| **CORS** | FastAPI에서 dashboard origin만 허용 |

---

## 9. Success Metrics

| 지표 | 목표 |
|------|------|
| **설치~첫 화면** | orchestrator에 `pip install` 후 5분 이내 dashboard 연동 |
| **파이프라인 가시성** | 실행 중인 파이프라인의 현재 스텝을 1초 이내에 UI 반영 |
| **명령 실행** | Dashboard에서 Solve 실행 → orchestrator 파이프라인 시작까지 2초 이내 |
| **텔레그램 대체율** | 기존 텔레그램 명령의 80%를 Dashboard에서 수행 가능 |

---

## 10. Out of Scope (v1)

- Multi-user / 팀 기능 (v1은 single-user)
- orchestrator 설정 변경 UI (`.env` 파일 직접 수정)
- AI 모델 관리 (Ollama pull/delete)
- Training data 뷰어 (`/extract` 결과)
- Figma 연동 UI (`/design`)
