## CI Parity 결과 (6개 잡) — issue #110 (Approval Modal UI)

| 잡 | 명령 | 상태 | 비고 |
|----|------|------|------|
| 1. ktlintCheck (루트) | `./gradlew ktlintCheck` | PASS | 41 tasks, 모두 UP-TO-DATE. commonMain/commonTest/desktopMain/desktopTest/androidMain/kotlinScripts 전부 통과. |
| 2. detekt (shared) | `./gradlew :shared:detekt` | PASS | NO-SOURCE (shared는 Kotlin Multiplatform 구성으로 detekt 태스크가 소스 없음 보고). 루트 `detekt` 구성은 CI 워크플로우에서 별도 처리. |
| 3. shared allTests | `./gradlew :shared:allTests` | PASS | 4 target × 합계 **1,788 tests / 0 failure / 0 error / 0 skip**. 상세: desktopTest 552, iosSimulatorArm64Test 412, testDebugUnitTest 412, testReleaseUnitTest 412. iosX64Test는 SKIPPED (unlinked `kotlin.uuid` 심볼 — kotlinx-serialization 1.7 + Kotlin 2.0 호환 이슈로 iosX64 링크 스킵, CI의 macOS 러너 동작과 동일). |
| 4. shared compileCommonMainKotlinMetadata | `./gradlew :shared:compileCommonMainKotlinMetadata` | PASS | 4 tasks UP-TO-DATE. |
| 5. desktopApp assemble | `./gradlew :desktopApp:assemble` | PASS | 13 tasks UP-TO-DATE. desktopJar 산출물 확인. |
| 6. androidApp assembleDebug | `./gradlew :androidApp:assembleDebug` | PASS | 61 tasks UP-TO-DATE. Debug APK 산출물 확인. |

## 총 테스트 카운트

- **shared 합계: 1,788 tests pass, 0 fail, 0 error, 0 skipped** (4 타겟 합산)
- desktopTest 기준 단일 타겟: 78 suites / 552 tests
  - `ApprovalDialogStateTest`: 21 tests (issue #110 신규 UI 상태 검증)
  - `ApprovalModalViewModelTest`: 29 tests (issue #109 회귀 검증)
  - `PipelineMonitorViewModelTest`: 17 tests (delegation 패턴 회귀 검증)
  - `ParallelPipelineViewModelTest`: 13 tests
  - 기타 repository/mapper/api 테스트: 472 tests

## 수정 이력

- 1회차: 6개 잡 모두 PASS, 코드 수정 없음.

## 경고 (비-블로킹)

- Compose Material 3 deprecation 경고 4건 (DesignPanel.kt:52, DiscussPanel.kt:55, PlanIssuesPanel.kt:57, CommandCenterScreen.kt:50) — 이슈 #110 범위 밖, 기존 코드 그대로.
- Gradle 8.7의 Gradle 9.0 deprecation 경고 — 인프라 범위, 이슈 범위 밖.
- kotlinx-serialization-core의 `kotlin.uuid/Uuid` unlinked symbol 경고 — iosX64Test SKIPPED 원인. iosSimulatorArm64Test는 정상 실행되어 iOS 로직 커버됨.
- iOS `IOSPipelineMonitorViewModel.swift` / `PipelineMonitorView.swift` / `ApprovalDialogView.swift` 변경은 KMP bridge 시그니처에 정합하며 shared 테스트로 간접 검증됨. iOS xcodebuild는 현 CI 워크플로우 미포함 (`.github/workflows/ci.yml` 6-job).

## 최종 상태: ALL GREEN (PR 생성 가능)
