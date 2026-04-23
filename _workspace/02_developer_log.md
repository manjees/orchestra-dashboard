# Developer Log — Issue #122 (Analytics Compose UI — 3 charts + period filter)

## 구현 완료 파일

### Shared (KMP) — Domain
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/domain/model/PeriodFilter.kt` — added `val label: String` computed property (WEEK→"Week", MONTH→"Month", ALL→"All")

### Shared (KMP) — UI Components (new)
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/PeriodFilterBar.kt` — FilterChip row for PeriodFilter.entries
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/SuccessRateChart.kt` — donut chart (Canvas + drawArc success/failure sweeps, "No runs" label for zero totalRuns) + stat rows
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/DurationTrendsChart.kt` — Canvas-based line chart with min/max normalization, dots + axis labels (first/mid/last dates)
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/StepFailureHeatmap.kt` — 5-bucket color heatmap (0.25 / 0.50 / 0.75 / 1.0 thresholds), sortedByDescending by failureRate

### Shared (KMP) — Screen (new)
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/screen/AnalyticsScreen.kt` — Scaffold + TopAppBar (back + refresh) + PeriodFilterBar + SuccessRateChart / DurationTrendsChart / StepFailureHeatmap in vertical scroll; loading/empty states

### Shared (KMP) — Navigation integration
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/screen/AppNavigation.kt` — added `Screen.Analytics` + `analyticsViewModelFactory` parameter + branch (DisposableEffect onCleared pattern)
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/screen/DashboardHomeScreen.kt` — added `onAnalyticsClick` + TopAppBar IconButton (`Icons.AutoMirrored.Filled.List`, since material-icons-core lacks BarChart)
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/screen/HistoryScreen.kt` — added `onAnalyticsClick` + TopAppBar IconButton (same icon)

### Shared (KMP) — Build
- [x] `shared/build.gradle.kts` — changed `implementation(libs.datetime)` → `api(libs.datetime)` so the `Clock.System` default in `AnalyticsViewModel` resolves at call sites in androidApp/desktopApp AppContainer without forcing them to add kotlinx-datetime explicitly

### Shared (KMP) — Tests
- [x] `shared/src/commonTest/kotlin/com/orchestradashboard/shared/ui/analytics/AnalyticsScreenStateTest.kt` — 15 tests (donut sweep math, zero runs label, duration trend ordering, 5 bucket threshold cases, PeriodFilter label non-blank)

### Android app
- [x] `androidApp/src/main/kotlin/com/orchestradashboard/android/di/AppContainer.kt` — removed `@Suppress("UnusedPrivateProperty")` from all 3 analytics use cases; added `AnalyticsViewModel` import + `createAnalyticsViewModel(project="default")` factory
- [x] `androidApp/src/main/kotlin/com/orchestradashboard/android/App.kt` — passes `analyticsViewModelFactory` to `AppNavigation`

### Desktop app
- [x] `desktopApp/src/main/kotlin/com/orchestradashboard/desktop/di/AppContainer.kt` — mirror of Android changes
- [x] `desktopApp/src/main/kotlin/com/orchestradashboard/desktop/Main.kt` — passes `analyticsViewModelFactory`

## 검증 결과

| 잡 | 상태 |
|---|---|
| `./gradlew :shared:compileKotlinDesktop :shared:compileCommonMainKotlinMetadata` | PASS |
| `./gradlew :shared:desktopTest` | PASS (AnalyticsScreenStateTest 15/15; pre-existing AnalyticsViewModelTest still passes) |
| `./gradlew :androidApp:compileDebugKotlin` | PASS (ANDROID_HOME=$HOME/Library/Android/sdk required locally) |
| `./gradlew :desktopApp:compileKotlin` | PASS |
| `./gradlew ktlintCheck` | PASS (ktlintFormat first auto-fixed DurationTrendsChart.kt single-line body) |
| `./gradlew detekt` | PASS |

## 설계 결정

- **Icons.Default.BarChart 미사용**: `material-icons-core` 모듈에 없음 (Info/List/Menu 등만 포함). `material-icons-extended` 의존성 추가 없이 `Icons.AutoMirrored.Filled.List`로 대체. 기존 HistoryScreen이 `DateRange` 로 우회한 것과 동일한 접근.
- **`api(libs.datetime)`**: `AnalyticsViewModel.clock: Clock = Clock.System` 기본값이 appContainer (androidApp/desktopApp) 호출 사이트에서 해석되어야 하므로 `shared`가 `kotlinx-datetime`을 public API로 노출. `Clock`은 이미 VM public constructor parameter이므로 `api`가 의미적으로도 맞음.
- **Bucket 함수 일치**: 테스트 헬퍼의 `failureBucket()`과 `StepFailureHeatmap.failureBucketColor()`가 동일한 `< 0.25 / < 0.50 / < 0.75 / < 1.0 / else` 경계를 사용.
- **`@Suppress("UnusedPrivateProperty")` 제거**: 3개 analytics use case가 이제 `createAnalyticsViewModel()`에서 실제로 사용됨.

## 미해결 이슈

- 없음
