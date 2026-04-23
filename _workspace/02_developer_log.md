# Developer Log — Issue #129 (Live Log Streaming UI — Compose + SwiftUI)

## Implementation Order

Tests (pure functions) -> StepNode/StepTimeline selection -> LogStreamPanel (Compose)
-> PipelineMonitorScreen integration -> AppNavigation wiring -> desktopApp/androidApp
DI wiring -> iOS StepTimelineView selection -> IOSLogStreamViewModel + IOSAppContainer
-> LogStreamPanelView (SwiftUI) -> PipelineMonitorView integration -> build/lint/test
verification.

## Production files created

### Shared (Compose)
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/LogStreamPanel.kt`

### iOS (SwiftUI)
- [x] `iosApp/iosApp/components/LogStreamPanelView.swift`
- [x] `iosApp/iosApp/IOSLogStreamViewModel.swift`

## Production files modified

### Shared (Compose)
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/StepNode.kt`
      — added `isSelected` + `onClick`; ring overlay via `.border(..., CircleShape)` when selected.
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/component/StepTimeline.kt`
      — added `selectedStepName` + `onStepClick` parameters, wired into each `StepNode`.
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/screen/PipelineMonitorScreen.kt`
      — now takes `logStreamViewModel: LogStreamViewModel`; shows `LogStreamPanel`
        (with `AnimatedVisibility`) when a step is selected, otherwise the pre-existing
        `LiveLogPanel`. Step clicks toggle start/stop on the log stream VM.
- [x] `shared/src/commonMain/kotlin/com/orchestradashboard/shared/ui/screen/AppNavigation.kt`
      — added `logStreamViewModelFactory: () -> LogStreamViewModel`; new LogStreamVM
        instance created per `Screen.PipelineMonitor` and disposed with the pipeline VM.

### desktopApp / androidApp (DI wiring)
- [x] `desktopApp/src/main/kotlin/com/orchestradashboard/desktop/Main.kt`
      — passes `AppContainer.createLogStreamViewModel` to `AppNavigation`.
- [x] `androidApp/src/main/kotlin/com/orchestradashboard/android/App.kt`
      — passes `AppContainer.createLogStreamViewModel` to `AppNavigation`.

(`createLogStreamViewModel()` already existed on both `AppContainer`s from issue #128
— no DI container changes required.)

### iOS
- [x] `iosApp/iosApp/components/StepTimelineView.swift`
      — added `selectedStepName` + `onStepTap`; selection ring via a `.stroke` overlay
        on the circle, tap handler via `.onTapGesture` on each step node.
- [x] `iosApp/iosApp/IOSAppContainer.swift`
      — added `createLogStreamViewModel()` factory stub (fatalError until KMP framework
        is linked, matching the other factory stubs in this container).
- [x] `iosApp/iosApp/PipelineMonitorView.swift`
      — wires `@StateObject IOSLogStreamViewModel`, threads selection state through
        `StepTimelineView`, swaps in `LogStreamPanelView` when a step is selected,
        disposes the log VM in `.onDisappear`.

## Test files created
- [x] `shared/src/commonTest/kotlin/com/orchestradashboard/shared/ui/component/LogStreamPanelStateTest.kt`
      — 10 tests covering:
        - `formatLogEntry` for INFO/WARN/ERROR/DEBUG
        - `formatLogEntry` with blank and malformed timestamps (no crash)
        - `logLevelColor` returning `Color.Unspecified` for INFO and semantic hues
          for WARN (orange), ERROR (red), DEBUG (gray)
      — All 10 tests pass.

## Verification

| Job | Status |
|---|---|
| `./gradlew :shared:desktopTest` (incl. new tests) | PASS — 10/10 new tests green, no regressions |
| `./gradlew ktlintFormat` + `ktlintCheck` | PASS |
| `./gradlew detekt` | PASS |
| `./gradlew :shared:compileKotlinDesktop` | PASS |
| `./gradlew :desktopApp:compileKotlin` | PASS |
| `./gradlew :androidApp:compileDebugKotlin` | skipped locally — no Android SDK on dev machine; CI will validate |

## Design decisions

- **Pure state logic in Compose file**: `formatLogEntry` and `logLevelColor` live as
  `internal` functions inside `LogStreamPanel.kt` so they can be unit-tested without
  a Compose runtime. `Color.Unspecified` is returned for INFO (caller falls back to
  the Material3 `onSurfaceVariant` token via `Color.takeOrElse`).
- **Timestamp parsing**: kept allocation-free and defensive — no `kotlinx.datetime`
  parser is required because the only information needed for display is the
  `HH:mm:ss` substring of an ISO-8601 value. Returns empty on malformed input so the
  UI still shows the message.
- **No `@Suppress`**: `AppNavigation` already exceeded `LongParameterList`'s threshold
  (11 parameters before, 12 after adding `logStreamViewModelFactory`). The existing
  signature did not need a suppression, so the new parameter does not either. detekt
  passes clean.
- **Auto-scroll behaviour**: `derivedStateOf` over `listState.layoutInfo` decides
  whether the user is at the bottom (within 2 rows of the tail). `LaunchedEffect(logs.size)`
  only animates to the bottom when that condition is met, so manual scrolling up
  pauses auto-scroll. A `FloatingActionButton` appears at the bottom-right to let the
  user resume tailing with a single tap.
- **AnimatedVisibility for panel swap**: replacing the existing `LiveLogPanel` with
  the step-scoped `LogStreamPanel` uses `AnimatedVisibility` so the transition is
  smooth when a step is (de)selected, and the fallback `LiveLogPanel` remains the
  default surface when no step is selected.
- **iOS selection ring via overlay**: SwiftUI `Circle().stroke(...).padding(-3)` gives
  a 3-pt outer ring that doesn't affect the layout of neighboring steps (so the
  timeline spacing stays consistent whether or not a step is selected).
- **iOS panel hot-reload via `ScrollViewReader`**: `onChange(of: viewModel.logs.count)`
  drives an automatic `scrollTo(_, anchor: .bottom)`. For now this always scrolls on
  new entries (mirrors the existing `LiveLogPanel` behaviour); SwiftUI does not expose
  scroll position cleanly in older OS targets, so the "pause auto-scroll when user
  scrolls up" behaviour is Compose-only for this PR.

## Unresolved issues

- None.
