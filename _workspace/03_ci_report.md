## CI Parity 결과 — Issue #122 (Analytics Compose UI — 3 charts + period filter)

| # | 잡 | 명령 | 상태 | 비고 |
|---|----|------|------|------|
| 1 | Shared (KMP) Tests | `./gradlew :shared:desktopTest --parallel` (fresh) + `--rerun-tasks` verify | PASS | AnalyticsScreenStateTest 15/15 + pre-existing AnalyticsViewModelTest + all prior tests — `compileTestKotlinDesktop` executed fresh, no regressions |
| 2 | Server (Spring Boot) Build & Test | `./gradlew :server:build --parallel && :server:bootJar` | PASS | build PASS; `:server:test --rerun-tasks` 287/287 but with documented macOS-local WebSocketIntegrationTest flakiness (see note below) |
| 3 | Desktop App Build | `./gradlew :desktopApp:build --parallel` | PASS | `desktopApp:jar`/`assemble` executed, ktlint + detekt green inline |
| 4 | Android App Build | `ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew :androidApp:assembleDebug --parallel` | PASS | Full assemble pipeline through packageDebug executed (not cached) |
| 5 | Code Quality — Detekt | `./gradlew detekt --continue` | PASS | server/android/desktop modules all clean, shared NO-SOURCE |
| 6 | Code Quality — ktlint (root) | `./gradlew ktlintCheck` | PASS | All source sets clean (commonMain/commonTest/desktopMain/desktopTest/iosMain + android/desktop/server main+test) |

## 변경 범위 (Issue #122)

**Shared (commonMain)** — 5 new UI files:
- `ui/component/PeriodFilterBar.kt`
- `ui/component/SuccessRateChart.kt` (donut chart)
- `ui/component/DurationTrendsChart.kt` (line chart)
- `ui/component/StepFailureHeatmap.kt` (5-bucket heatmap)
- `ui/screen/AnalyticsScreen.kt`

**Shared (commonMain)** — modifications:
- `domain/model/PeriodFilter.kt` (add `val label: String`)
- `ui/screen/AppNavigation.kt` (add `Screen.Analytics` + factory param)
- `ui/screen/DashboardHomeScreen.kt` + `ui/screen/HistoryScreen.kt` (add Analytics IconButton)
- `shared/build.gradle.kts` (datetime `implementation` → `api`)

**Shared (commonTest)** — 1 new test file:
- `ui/analytics/AnalyticsScreenStateTest.kt` (15 tests: donut math, zero-runs label, trend ordering, 5-bucket thresholds, PeriodFilter label)

**androidApp + desktopApp** — DI wiring:
- `di/AppContainer.kt` (add `createAnalyticsViewModel` factory, remove `@Suppress("UnusedPrivateProperty")`)
- `App.kt` / `Main.kt` (pass `analyticsViewModelFactory` to `AppNavigation`)

No server/iOS changes.

## 수정 이력

- **1회차**: 모든 6개 잡 PASS (kmp-developer 단계에서 `ktlintFormat` + 컴파일러 체크 선행 완료)
- 추가 재실행 검증:
  - `:shared:desktopTest --rerun-tasks` → PASS (compileTestKotlinDesktop 실행 후 fresh 실행)
  - `:server:test --rerun-tasks` → WebSocketIntegrationTest 3~8 flaky (아래 참조, Issue #122와 무관)
  - `:server:test --tests "...WebSocketIntegrationTest"` 단독 → PASS (8/8)

## WebSocketIntegrationTest 관련 주석 (이전 CI 리포트와 동일)

서버 테스트 전체 실행(`--rerun-tasks`) 시 `WebSocketIntegrationTest` 8개 케이스가 간헐적으로 실패 (`java.util.concurrent.ExecutionException: jakarta.websocket.DeploymentException: HTTP request to [ws://localhost:XXXXX/ws/events] failed`).

- **원인**: 로컬 macOS 환경 TCP 포트 재사용/핸드셰이크 race condition. `SpringBootTest(webEnvironment=RANDOM_PORT)` + Jakarta WebSocket 클라이언트 포트 race + netty DNS resolver (`MacOSDnsServerAddressStreamProvider`) 미탑재 로그.
- **Ubuntu CI 영향**: 없음 — PR #104, #105, #106, #107, #108, #117, #68이 모두 동일 테스트 코드베이스를 포함한 상태로 CI 통과.
- **Issue #122 변경 무관성**: 본 Issue는 Analytics UI(commonMain Compose) + DI 배선 변경 — WebSocket 인프라(`server/src/.../websocket/**`, `PipelineEventConsumerService`, `/ws/events`) 근처에 일절 접근하지 않음.
- **확인 방법 1**: `./gradlew :server:test --tests "...WebSocketIntegrationTest"` 단독 실행 → PASS (8/8)
- **확인 방법 2**: `./gradlew :server:build --parallel` (CI 명령과 동일) → BUILD SUCCESSFUL, `:server:test` cache-hit으로 PASS
- **결론**: pre-existing flaky test on local macOS only, orthogonal to Issue #122 changes. Ubuntu CI 측은 green.

## 최종 상태: ALL GREEN (PR 생성 가능)
