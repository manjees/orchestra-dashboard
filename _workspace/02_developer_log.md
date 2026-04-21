# Developer Log — Issue #110 (Approval Modal UI)

## 구현 완료 파일

### shared/commonTest (TDD — test-first)
- [x] `shared/src/commonTest/kotlin/com/orchestradashboard/shared/ui/component/ApprovalDialogStateTest.kt` — 19개의 T-UI-N 테스트 + sanity 1개 = 총 20 tests (실제 JUnit report 기준 21 testcases, T-UI-11a/11b 분리 영향)

### shared/commonMain
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/ApprovalDialog.kt`
  - `onRespond: (decision: String) -> Unit` → `onRespond: (ApprovalDecision) -> Unit`
  - `isSubmitting`, `error`, `onClearError` 파라미터 추가
  - 에러 메시지 표시 + `CircularProgressIndicator` 로딩 표시
  - 모든 액션 버튼에 `enabled = !isSubmitting` 전달
  - `buildTitle` → `internal buildDialogTitle`로 rename + 공개
  - 새 internal 함수 추가: `formatCountdownText`, `calculateProgress`, `approvalDecisionsForType`
  - StrategyButtons/SupremeCourtButtons/GenericButtons가 타입-세이프한 `ApprovalDecision` sealed 인스턴스 전달
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/screen/PipelineMonitorScreen.kt`
  - 사용하지 않는 `GenericDecision` import 제거
  - `ApprovalDialog`에 `isSubmitting`, `error`, `onClearError` 전달
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/pipelinemonitor/PipelineMonitorViewModel.kt`
  - `GenericDecision` import 추가
  - iOS bridge용 메서드 3개 추가: `respondToApproval(decision, comment)`, `dismissApproval()`, `clearApprovalError()`

### iosApp
- [x] `iosApp/iosApp/components/ApprovalDialogView.swift`
  - `isSubmitting`, `error`, `onClearError` 프로퍼티 추가
  - toolbar에 로딩 `ProgressView` 추가, `.alert` 바인딩으로 에러 표시
  - 모든 액션 버튼에 `.disabled(isSubmitting)` 적용
- [x] `iosApp/iosApp/IOSPipelineMonitorViewModel.swift`
  - `isApprovalSubmitting`, `approvalError` @Published 속성 추가
  - `approvalCollectTask`로 `approvalModal.uiState` 별도 수집
  - `updateFromState`에서 approval 필드(`pendingApproval`, `remainingTimeSec`, `isApprovalTimedOut`) 제거 — 더 이상 PipelineMonitorUiState에 존재하지 않음
  - 신규 `updateFromApprovalState(ApprovalModalState)` 추가
  - `respondToApproval`을 KMP bridge 메서드로 라우팅
  - `clearApprovalError()` 추가
  - `onCleared()`에서 `approvalCollectTask` 함께 취소
  - 파일 하단에 `ApprovalUiStateCollector` private 클래스 추가
- [x] `iosApp/iosApp/PipelineMonitorView.swift`
  - `.sheet` 블록에 `isSubmitting`, `error`, `onClearError` 전달

### desktopApp / androidApp (AppContainer 수정 — 기존 compile-break 동반 수정)
- [x] `desktopApp/src/main/kotlin/com/orchestradashboard/desktop/di/AppContainer.kt`
  - `ApprovalModalViewModel` import 추가
  - `createPipelineMonitorViewModel`: 구 시그니처 `(pipelineId, repo, useCase, mapper)` → 신 시그니처 `(pipelineId, repository, approvalModal=ApprovalModalViewModel(useCase, mapper))`
- [x] `androidApp/src/main/kotlin/com/orchestradashboard/android/di/AppContainer.kt`
  - 동일 패턴 수정 (desktop과 동일하게 `ApprovalModalViewModel` 주입)

## 검증 결과

- `:shared:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL
- `:shared:desktopTest` — BUILD SUCCESSFUL, **78 suites / 552 tests / 0 failures / 0 errors / 0 skipped**
  - `ApprovalDialogStateTest`: 21/21 pass (19 T-UI-N + T-UI-11a/11b split + sanity)
  - `ApprovalModalViewModelTest`: 29/29 pass (회귀 없음)
  - `PipelineMonitorViewModelTest`: 17/17 pass (회귀 없음)
  - `ParallelPipelineViewModelTest`: 13/13 pass
- `:desktopApp:assemble` — BUILD SUCCESSFUL
- `:androidApp:assembleDebug` — BUILD SUCCESSFUL
- `ktlintCheck` (root, 전체 모듈) — BUILD SUCCESSFUL
- `:shared:detekt` — BUILD SUCCESSFUL (NO-SOURCE)

## 미해결 이슈

- 없음 (iOS CI는 현 워크플로우에 미포함 — `.github/workflows/ci.yml` 6-job. iOS Swift 변경은 문법적으로 정합하나 xcodebuild 미실행. KMP bridge 시그니처는 shared 테스트로 검증됨)
- Compose Material 3 deprecation 경고 4건 (DesignPanel/DiscussPanel/PlanIssuesPanel/CommandCenterScreen) — 이슈 #110 범위 밖, 기존 경고 그대로 유지
