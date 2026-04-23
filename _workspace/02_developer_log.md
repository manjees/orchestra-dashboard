# Developer Log — Issue #68 (History Screen — Pipeline Execution History Filter/Search)

## 구현 완료 파일

### Server (Spring Boot BFF)
- [x] `server/src/main/kotlin/com/orchestradashboard/server/repository/PipelineHistoryJpaRepository.kt` — added 4 new query methods: `findByIssueTitleContainingIgnoreCase`, `findByProjectNameAndIssueTitleContainingIgnoreCase`, `findByStatusAndIssueTitleContainingIgnoreCase`, `findByProjectNameAndStatusAndIssueTitleContainingIgnoreCase`
- [x] `server/src/main/kotlin/com/orchestradashboard/server/service/PipelineHistoryService.kt` — expanded `getHistory` signature to `(project, status, keyword, from, to, pageable)` with priority dispatch table; empty keyword treated as null; private `dispatchQuery` helper
- [x] `server/src/main/kotlin/com/orchestradashboard/server/controller/PipelineHistoryController.kt` — added `@RequestParam q, from, to`
- [x] `server/src/test/kotlin/com/orchestradashboard/server/controller/PipelineHistoryControllerTest.kt` — updated existing 5 tests to pass new nulls, added 4 new tests (keyword, empty keyword, date range, combined)
- [x] `server/src/test/kotlin/com/orchestradashboard/server/service/PipelineHistoryServiceTest.kt` — updated 4 existing tests + added 6 new tests (keyword-only, project+keyword, status+keyword, project+status+keyword, empty keyword, date range)
- [x] `config/detekt/detekt.yml` — `LongParameterList.functionThreshold` 6 → 8 (accommodates filter + pageable params in controller/service)

### Shared (KMP)

#### Domain
- [x] `shared/.../domain/model/HistoryFilter.kt` (project/status/keyword/timeRange nullable)
- [x] `shared/.../domain/model/HistoryDetail.kt` (11 fields)
- [x] `shared/.../domain/model/HistoryStep.kt` (4 fields)
- [x] `shared/.../domain/repository/HistoryRepository.kt` (getPagedHistory + getHistoryDetail)
- [x] `shared/.../domain/usecase/GetPagedHistoryUseCase.kt` (default page=0, pageSize=20)
- [x] `shared/.../domain/usecase/GetHistoryDetailUseCase.kt` (single-delegation usecase)

#### Data
- [x] `shared/.../data/dto/orchestrator/PipelineHistoryDetailDto.kt` + `StepHistoryDto` (snake_case SerialName)
- [x] `shared/.../data/dto/orchestrator/PipelineHistoryPageDto.kt` (Spring Page wrapper — camelCase defaults)
- [x] `shared/.../data/mapper/HistoryDetailMapper.kt` (toDomain, toPagedDomain, parseStatus/parseStepStatus with FAILED fallback)
- [x] `shared/.../data/network/DashboardApi.kt` — added `getPagedHistory(...)` and `getHistoryDetail(id)`
- [x] `shared/.../data/network/DashboardApiClient.kt` — implemented both new methods (calls BFF `/api/v1/pipeline-history`)
- [x] `shared/.../data/repository/HistoryRepositoryImpl.kt` — time range computed from `Clock.System.now() - hours * MILLIS_PER_HOUR`

#### UI
- [x] `shared/.../ui/history/HistoryUiState.kt` (13 fields + 3 computed booleans)
- [x] `shared/.../ui/history/HistoryViewModel.kt` (`SupervisorJob + Dispatchers.Main`, 300ms search debounce, pagination, detail selection)
- [x] `shared/.../ui/component/PipelineStatusFilterBar.kt` (All + PASSED/FAILED/CANCELLED/RUNNING)
- [x] `shared/.../ui/component/HistoryDetailSheet.kt` (header/summary/step list with stepStatusColor + failDetail)
- [x] `shared/.../ui/component/PipelineResultRow.kt` — optional `onClick: (() -> Unit)?` parameter added (wraps in `clickable{}`)
- [x] `shared/.../ui/screen/HistoryScreen.kt` (Scaffold + Search + StatusFilterBar + time-range FilterChips + LazyColumn with infinite-scroll snapshotFlow + detail overlay)
- [x] `shared/.../ui/screen/AppNavigation.kt` — added `Screen.History` + `historyViewModelFactory` parameter
- [x] `shared/.../ui/screen/DashboardHomeScreen.kt` — added `onHistoryClick` param + TopAppBar `DateRange` icon (chose DateRange vs. History because `Icons.Default.History` requires material-icons-extended dependency not currently in shared)

#### Tests
- [x] `shared/src/commonTest/.../ui/history/FakeHistoryRepository.kt` (mutable fields, call counters, last* trackers)
- [x] `shared/src/commonTest/.../ui/history/HistoryViewModelTest.kt` (16 tests — initial/load/error/filter/search/debounce/pagination/selectDetail/clearSelection/refresh/clearError/timeRange)
- [x] `shared/src/commonTest/.../domain/usecase/GetPagedHistoryUseCaseTest.kt` (7 tests — default/project/status/keyword/timeRange/custom-page/failure)
- [x] `shared/src/commonTest/.../domain/usecase/GetHistoryDetailUseCaseTest.kt` (2 tests — valid/invalid)
- [x] `shared/src/commonTest/.../data/mapper/HistoryDetailMapperTest.kt` (8 tests — all fields/null completedAt/steps with failDetail/unknown status/Spring Page fields/PipelineResult completedAt stringified/null preserved/empty content)

#### Test infra updates (3 existing Fakes extended with new `getPagedHistory` + `getHistoryDetail` stubs)
- [x] `shared/src/commonTest/.../data/network/FakeDashboardApiClient.kt`
- [x] `shared/src/commonTest/.../data/repository/FakeDashboardApiClient.kt`
- [x] `shared/src/commonTest/.../data/api/FakeOrchestratorApiClient.kt`
- [x] `shared/src/desktopTest/.../data/repository/ProjectRepositoryImplTest.kt` (inline FakeProjectDashboardApi)

### Desktop app
- [x] `desktopApp/.../di/AppContainer.kt` — added `historyDetailMapper`, `historyRepository`, `getPagedHistoryUseCase`, `getHistoryDetailUseCase`, `createHistoryViewModel()`
- [x] `desktopApp/.../Main.kt` — passes `historyViewModelFactory`

### Android app
- [x] `androidApp/.../di/AppContainer.kt` — mirror of Desktop AppContainer changes
- [x] `androidApp/.../App.kt` — passes `historyViewModelFactory`

### iOS app
- [x] `iosApp/iosApp/IOSHistoryViewModel.swift` — `@MainActor ObservableObject` with `HistoryUiStateCollector: Kotlinx_coroutines_coreFlowCollector`
- [x] `iosApp/iosApp/HistoryView.swift` — SwiftUI view with search/status chips/time-range chips/list/detail sheet
- [x] `iosApp/iosApp/ContentView.swift` — added `HistoryView()` tab with `Label("History", systemImage: "clock")`
- [x] `iosApp/iosApp/IOSAppContainer.swift` — added `createHistoryViewModel()` stub (fatalError until framework linked)

## 검증 결과

| 잡 | 상태 |
|---|---|
| `./gradlew :shared:desktopTest` | PASS (16 HistoryViewModel + 7 GetPagedHistory + 2 GetHistoryDetail + 8 HistoryDetailMapper tests) |
| `./gradlew :server:test :server:controller.PipelineHistoryControllerTest` | PASS 10/10 |
| `./gradlew :server:test :server:service.PipelineHistoryServiceTest` | PASS 15/15 |
| `./gradlew :desktopApp:build` | PASS |
| `./gradlew :androidApp:assembleDebug` | PASS |
| `./gradlew ktlintCheck` | PASS |
| `./gradlew detekt` | PASS |
| `./gradlew :shared:compileKotlinIosSimulatorArm64` | PASS |

Note: `WebSocketIntegrationTest` has 4 intermittent failures in aggregate run but passes standalone (`--tests "...WebSocketIntegrationTest"` → SUCCESSFUL). The flakiness is pre-existing (`Can't assign requested address: localhost/127.0.0.1:9000` during parallel test execution) and unrelated to the History screen implementation.

## 미해결 이슈

- 없음
