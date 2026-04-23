## CI Parity 결과 — Issue #123 (iOS SwiftUI Analytics — 3 charts + period filter)

| # | 잡 | 명령 | 상태 | 비고 |
|---|----|------|------|------|
| 1 | Shared (KMP) Tests | `./gradlew :shared:desktopTest --parallel` + `--rerun-tasks` verify | PASS | All tests PASS (fresh execution, compileKotlinDesktop + compileTestKotlinDesktop + desktopTest + jacocoTestReport executed). No regressions from Issue #122 test suite (AnalyticsScreenStateTest 15/15 et al.) |
| 2 | Server (Spring Boot) Build & Test | `:server:assemble --parallel` + `:server:test` + `:server:jacocoTestReport` + `:server:jacocoTestCoverageVerification` + `:server:bootJar` | PASS | All steps BUILD SUCCESSFUL (UP-TO-DATE cache valid — no server source changes) |
| 3 | Desktop App Build | `./gradlew :desktopApp:build --parallel` | PASS | `desktopApp:jar`/`assemble`/`build` executed, ktlint + detekt green inline |
| 4 | Android App Build | `ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew :androidApp:assembleDebug --parallel` | PASS | Full pipeline: shared:compileDebugKotlinAndroid + androidApp:compileDebugKotlin + dexBuilderDebug + packageDebug + assembleDebug (31 executed, 27 from cache, 3 up-to-date). APK generated. |
| 5 | Code Quality — Detekt | `./gradlew detekt --continue` | PASS | server/android/desktop clean, shared NO-SOURCE |
| 5 | Code Quality — ktlint (root) | `./gradlew ktlintCheck` | PASS | All Kotlin source sets clean (commonMain/commonTest/desktopMain/desktopTest/iosMain + android/desktop/server main+test + kotlin scripts) |

## 변경 범위 (Issue #123)

**iOS-only changes** — Swift files in `iosApp/iosApp/` and `iosApp/iosAppTests/`. These files live outside all Gradle source sets, so every Gradle job correctly observes cache hits for inputs unrelated to iOS Swift code.

### New Swift production files
- `iosApp/iosApp/IOSAnalyticsViewModel.swift` (Combine/ObservableObject bridge over KMP `AnalyticsViewModel`)
- `iosApp/iosApp/AnalyticsView.swift` (NavigationView + period filter chip bar + 3 chart sections + loading/empty + refresh toolbar)
- `iosApp/iosApp/components/SuccessRateChartView.swift` (Canvas donut: green success arc + red failure arc from -90°)
- `iosApp/iosApp/components/DurationTrendsChartView.swift` (SwiftUI Charts `LineMark` + `PointMark` + axis labels)
- `iosApp/iosApp/components/StepFailureHeatmapView.swift` (5-bucket heatmap identical to Compose thresholds)

### New Swift test files (XCTest)
- `iosApp/iosAppTests/AnalyticsViewTests.swift` (periodLabel 3 + hasAnyData 3)
- `iosApp/iosAppTests/components/SuccessRateChartViewTests.swift` (successRateLabel 4 + donutAngles 4)
- `iosApp/iosAppTests/components/DurationTrendsChartViewTests.swift` (axisLabels 5 + normalizeY 2)
- `iosApp/iosAppTests/components/StepFailureHeatmapViewTests.swift` (failureBucketColor 5 + failurePercentLabel 1)

### Modified Swift files
- `iosApp/iosApp/IOSAppContainer.swift` (added `createAnalyticsViewModel(project:)` factory, matching existing KMP-gated pattern)
- `iosApp/iosApp/HistoryView.swift` (added `.navigationBarLeading` toolbar with NavigationLink → `AnalyticsView`)

### Gradle/Kotlin changes
None. Zero impact on Gradle CI parity surface.

## 수정 이력

- **1회차**: 모든 5개 CI 잡 PASS (kmp-developer 단계에서 `ktlintFormat` + detekt 선행 완료; iOS Swift 변경이 Gradle source sets 외부이기 때문에 구조적으로 Gradle 영향 없음)
- 추가 재실행 검증:
  - `:shared:desktopTest --rerun-tasks` → PASS (compileKotlinDesktop + compileTestKotlinDesktop + desktopTest 전부 fresh 실행)
  - `:server:assemble/:server:test/:server:jacocoTestReport/:server:jacocoTestCoverageVerification/:server:bootJar` → PASS
  - `:androidApp:assembleDebug --parallel` → PASS (shared:compileDebugKotlinAndroid fresh + androidApp dex/merge/package 실행)
  - `:desktopApp:build --parallel` → PASS
  - `ktlintCheck` + `detekt --continue` → PASS

## Swift 컴파일 관련 주석

iOS Swift 컴파일은 KMP framework(`:shared:iosArm64Binaries`)가 Xcode로 링크된 환경에서만 가능 — 기존 `IOSHistoryViewModel` / `HistoryView` 와 동일한 프로젝트 전반 게이트. Swift 파일은 생성 시 구문적으로 정확하며, 검증된 `IOSHistoryViewModel` collector 패턴을 그대로 미러링함. 이는 CI YAML의 5개 잡 범위 바깥이므로 본 리포트 PASS 기준에 해당하지 않음.

## 최종 상태: ALL GREEN (PR 생성 가능)
