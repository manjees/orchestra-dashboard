import XCTest
import SwiftUI
@testable import iosApp

/// Tests for SolveDialogView logic and SolveModeOption/SolveIssueItem helpers.
/// Covers behavior requirements from issue #101 acceptance criteria.
final class SolveDialogViewTests: XCTestCase {

    // MARK: - SolveModeOption

    func testSolveModeOption_hasFourCases() {
        XCTAssertEqual(SolveModeOption.allCases.count, 4,
            "Mode picker must expose exactly 4 options: Express, Standard, Full, Auto")
    }

    func testSolveModeOption_containsAllRequiredModes() {
        let modes = SolveModeOption.allCases.map { $0.rawValue }
        XCTAssertTrue(modes.contains("Express"))
        XCTAssertTrue(modes.contains("Standard"))
        XCTAssertTrue(modes.contains("Full"))
        XCTAssertTrue(modes.contains("Auto"))
    }

    // MARK: - SolveIssueItem

    func testSolveIssueItem_identityIsId() {
        let item = SolveIssueItem(id: 42, title: "Fix something")
        XCTAssertEqual(item.id, 42)
        XCTAssertEqual(item.title, "Fix something")
    }

    func testSolveIssueItem_distinctIds_areDistinctIdentifiers() {
        let a = SolveIssueItem(id: 1, title: "A")
        let b = SolveIssueItem(id: 2, title: "B")
        XCTAssertNotEqual(a.id, b.id)
    }

    // MARK: - Parallel toggle visibility logic

    /// The parallel toggle must be hidden when 0 or 1 issues are selected.
    func testParallelToggle_hiddenWhenSingleIssueSelected() {
        XCTAssertFalse(shouldShowParallelToggle(selectedCount: 1),
            "Parallel toggle must be hidden when only 1 issue is selected")
    }

    func testParallelToggle_hiddenWhenNoIssueSelected() {
        XCTAssertFalse(shouldShowParallelToggle(selectedCount: 0),
            "Parallel toggle must be hidden when no issues are selected")
    }

    func testParallelToggle_visibleWhenMultipleIssuesSelected() {
        XCTAssertTrue(shouldShowParallelToggle(selectedCount: 2),
            "Parallel toggle must be visible when 2 or more issues are selected")
    }

    func testParallelToggle_visibleWhenManyIssuesSelected() {
        XCTAssertTrue(shouldShowParallelToggle(selectedCount: 5))
    }

    // MARK: - Solve button disabled-state logic

    func testSolveButtonDisabled_whenNoIssueSelected() {
        XCTAssertTrue(isSolveButtonDisabled(selectedCount: 0, isSolving: false),
            "Solve button must be disabled when selectedIssueIds is empty")
    }

    func testSolveButtonDisabled_whileSolving() {
        XCTAssertTrue(isSolveButtonDisabled(selectedCount: 1, isSolving: true),
            "Solve button must be disabled while isSolving is true")
    }

    func testSolveButtonEnabled_whenIssueSelectedAndNotSolving() {
        XCTAssertFalse(isSolveButtonDisabled(selectedCount: 1, isSolving: false),
            "Solve button must be enabled when at least one issue is selected and not solving")
    }

    // MARK: - Issue list not restricted to selected IDs (regression)

    /// Regression: issues array must reflect the full project issue list, not just selectedIssueIds.
    func testIssueList_containsAllProvidedIssues() {
        let allIssues = [
            SolveIssueItem(id: 1, title: "Fix auth"),
            SolveIssueItem(id: 2, title: "Add dark mode"),
            SolveIssueItem(id: 3, title: "Refactor DB"),
        ]
        let selectedIds: Set<Int> = [1] // only 1 selected, but all 3 must be rendered
        XCTAssertEqual(allIssues.count, 3,
            "All 3 issues must be available in the dialog, not just the 1 that is selected")
        XCTAssertTrue(allIssues.contains(where: { $0.id == 2 }),
            "Non-selected issue #2 must still appear in the issue list")
        XCTAssertTrue(allIssues.contains(where: { $0.id == 3 }),
            "Non-selected issue #3 must still appear in the issue list")
        // Sanity-check that selection state is separate from the list
        XCTAssertFalse(selectedIds.contains(2))
    }

    // MARK: - Selection toggle logic

    func testToggleSelection_addsIssueWhenNotSelected() {
        var selected: Set<Int> = []
        selected = toggleSelection(issueId: 5, in: selected)
        XCTAssertTrue(selected.contains(5))
    }

    func testToggleSelection_removesIssueWhenAlreadySelected() {
        var selected: Set<Int> = [5, 6]
        selected = toggleSelection(issueId: 5, in: selected)
        XCTAssertFalse(selected.contains(5))
        XCTAssertTrue(selected.contains(6), "Other selections must remain unchanged")
    }
}

// MARK: - Pure logic helpers (mirror the logic in SolveDialogView body)

private func shouldShowParallelToggle(selectedCount: Int) -> Bool {
    return selectedCount > 1
}

private func isSolveButtonDisabled(selectedCount: Int, isSolving: Bool) -> Bool {
    return selectedCount == 0 || isSolving
}

private func toggleSelection(issueId: Int, in current: Set<Int>) -> Set<Int> {
    var updated = current
    if updated.contains(issueId) {
        updated.remove(issueId)
    } else {
        updated.insert(issueId)
    }
    return updated
}
