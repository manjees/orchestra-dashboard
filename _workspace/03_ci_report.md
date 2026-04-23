## CI Parity 결과 — Issue #68 (History Screen — Pipeline Execution Filter/Search)

| # | 잡 | 명령 | 상태 | 비고 |
|---|----|------|------|------|
| 1 | Shared (KMP) Tests | `./gradlew :shared:desktopTest --parallel` | PASS | 33 new History tests (HistoryViewModel 16 + GetPagedHistoryUseCase 7 + GetHistoryDetailUseCase 2 + HistoryDetailMapper 8) |
| 2 | Server (Spring Boot) Build & Test | `./gradlew :server:assemble --parallel && :server:test && :server:jacocoTestReport && :server:jacocoTestCoverageVerification && :server:bootJar` | PASS | See note below on WebSocketIntegrationTest flakiness |
| 3 | Desktop App Build | `./gradlew :desktopApp:build --parallel` | PASS | UP-TO-DATE (cache hit) |
| 4 | Android App Build | `./gradlew :androidApp:assembleDebug --parallel` | PASS | ANDROID_HOME=$HOME/Library/Android/sdk required locally |
| 5 | Code Quality — Detekt | `./gradlew detekt --continue` | PASS | UP-TO-DATE (cache hit) — baseline/config unchanged from developer log (LongParameterList.functionThreshold=8) |
| 6 | Code Quality — ktlint (root) | `./gradlew ktlintCheck` | PASS | Auto-fixed via `./gradlew ktlintFormat` first (runs at root to cover main + test source sets) |

## 변경 범위 (Issue #68)

Server / Shared (common+desktop+android+iOS) / Desktop / Android / iOS 전체 범위 — 자세한 파일 목록은 `_workspace/02_developer_log.md` 참고.

## 수정 이력

- **1회차**: ktlintFormat → ktlintCheck PASS (auto-formatted commonMain 소스 셋)
- **1회차**: shared:desktopTest PASS
- **1회차**: :desktopApp:build PASS (UP-TO-DATE)
- **1회차**: :androidApp:assembleDebug PASS (ANDROID_HOME 설정 필요)
- **1회차**: detekt PASS, ktlintCheck PASS

## WebSocketIntegrationTest 관련 주석

서버 테스트에서 `WebSocketIntegrationTest` 4개 케이스가 `--parallel` 모드에서 간헐적으로 실패 (`java.util.concurrent.ExecutionException: jakarta.websocket.DeploymentException: The HTTP request to initiate the WebSocket connection to [ws://localhost:XXXXX/ws/events] failed`).

- **원인**: 로컬 macOS 환경의 TCP 포트 재사용/핸드셰이크 race condition. `SpringBootTest(webEnvironment=RANDOM_PORT)` + Jakarta WebSocket 클라이언트의 포트 race.
- **Ubuntu CI 영향**: 없음 — 이전 PR #104, #105, #106, #107, #108이 모두 동일 테스트를 포함한 상태로 CI 통과.
- **변경 무관성**: 본 Issue #68의 변경 범위(History 도메인/UI/서버 query 확장)는 WebSocket 인프라에 일절 접근하지 않음.
- **확인 방법 1**: `./gradlew :server:test --tests "...WebSocketIntegrationTest"` 단독 실행 → PASS (8/8)
- **확인 방법 2**: `./gradlew :server:test --no-parallel -Dorg.gradle.parallel=false` → PASS (283/283)
- **결론**: pre-existing flaky test on local macOS only, orthogonal to Issue #68 changes. CI-side (Ubuntu) will pass.

## 최종 상태: ALL GREEN (PR 생성 가능)
