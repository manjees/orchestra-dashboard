# Developer Log — Issue #114 (Approval Modal — SwiftUI iosApp)

## 구현 완료 파일

### iosApp (신규)
- [x] `iosApp/iosApp/IOSApprovalModalViewModel.swift`
  - `@MainActor final class` — `ObservableObject` 래퍼
  - `@Published` 6개: `showDialog`, `pendingApproval`, `remainingTimeSec`, `isTimedOut`, `isSubmitting`, `approvalError`
  - `ApprovalModalStateCollector: Kotlinx_coroutines_coreFlowCollector` (IOSPipelineMonitorViewModel 패턴 동일)
  - `respond(decision:comment:)` — `ApprovalDecisionValue` → `ApprovalDecision` sealed 매핑
    - strategy 3개: `StrategyDecision.splitExecute/.noSplit/.cancel`
    - supreme court 3개: `SupremeCourtDecision.uphold/.overturn/.redesign`
    - generic 2개: `GenericDecision(value: "approve"|"reject")`
    - fallback: `GenericDecision(value: decision.value)`
  - `dismiss()`, `clearError()`, `onCleared()` — KMP ViewModel 메서드 위임
  - `remainingTimeSec`: `KotlinInt?` → `Int32?`를 `.int32Value`로 변환 (기존 패턴)

- [x] `iosApp/iosApp/ApprovalModalView.swift`
  - `@ObservedObject var viewModel: IOSApprovalModalViewModel`
  - `.sheet(isPresented: Binding)` — 양방향 바인딩, 사용자 dismiss 제스처도 `viewModel.dismiss()` 호출
  - sheet 안에서 `ApprovalDialogView` 렌더링 (기존 stateless 컴포넌트 재사용)
  - `#Preview` 매크로 4종: Strategy active / Supreme Court active / Timed out / Submitting

### iosApp (수정)
- [x] `iosApp/iosApp/IOSAppContainer.swift`
  - `createApprovalModalViewModel() -> ApprovalModalViewModel` 팩토리 추가
  - 기존 다른 ViewModel 팩토리와 동일한 `fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")` 스텁 패턴

- [x] `iosApp/iosApp/PipelineMonitorView.swift`
  - 기존 inline `.sheet` 블록에 유지 사유 NOTE 주석 추가
  - 의도적으로 교체하지 않음: `PipelineMonitorView`는 `IOSPipelineMonitorViewModel` → KMP `PipelineMonitorViewModel.approvalModal`을 사용하고 있으며, 신규 `IOSApprovalModalViewModel`로 교체 시 별도 `ApprovalModalViewModel` 인스턴스가 생성되어 pipeline events를 받지 못함
  - 주석으로 `ApprovalModalView`는 `PipelineMonitorViewModel`을 소유하지 않는 화면에서만 사용할 것을 명시

## 검증 결과 (정적)

- iOS CI는 본 저장소 `.github/workflows/ci.yml` 6-job 구성에서 미포함 (Issue #110 기준 동일)
- Swift 문법은 기존 `IOSPipelineMonitorViewModel` / `PipelineMonitorView` / `ApprovalDialogView` 패턴과 정합:
  - `Kotlinx_coroutines_coreFlowCollector` 심볼 사용 동일
  - `@MainActor` + `Task { @MainActor [weak self] in ... }` 수집 패턴 동일
  - `.int32Value` 언래핑 동일
  - `ApprovalDecisionValue` switch는 기존 `StepTimelineView`의 `switch status { case .pending: ... default: ... }` 컨벤션 준수
- KMP bridge 타입 확인:
  - `ApprovalModalState.showDialog: Bool` ✓
  - `ApprovalModalState.remainingTimeSec: Int?` → Swift `KotlinInt?` ✓
  - `ApprovalModalState.isTimedOut: Bool` (computed property) ✓
  - `ApprovalDecision` sealed interface, `StrategyDecision`/`SupremeCourtDecision`/`GenericDecision(value:)` 구현 ✓
  - `ApprovalModalViewModel.respond(decision:comment:)` 시그니처 확인 ✓

## 미해결 이슈

- 없음
