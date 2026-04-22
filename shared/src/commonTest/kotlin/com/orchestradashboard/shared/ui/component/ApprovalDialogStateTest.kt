package com.orchestradashboard.shared.ui.component

import com.orchestradashboard.shared.domain.model.ApprovalContext
import com.orchestradashboard.shared.domain.model.ApprovalRequest
import com.orchestradashboard.shared.domain.model.GenericDecision
import com.orchestradashboard.shared.domain.model.StrategyDecision
import com.orchestradashboard.shared.domain.model.SupremeCourtDecision
import com.orchestradashboard.shared.ui.approvalmodal.ApprovalModalState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApprovalDialogStateTest {
    private fun sampleApproval(approvalType: String = "strategy"): ApprovalRequest =
        ApprovalRequest(
            approvalType = approvalType,
            options = emptyList(),
            id = "approval-1",
            context = null,
            timeoutSec = 60,
            requestedAtMs = 0L,
        )

    // ─── T-UI-1 ~ T-UI-3 : buildDialogTitle ────────────────────────────────────

    @Test
    fun `T-UI-1 buildDialogTitle returns strategy title`() {
        assertEquals("Strategy Approval Required", buildDialogTitle("strategy"))
    }

    @Test
    fun `T-UI-2 buildDialogTitle returns supreme court title`() {
        assertEquals("Supreme Court Review Required", buildDialogTitle("supreme_court"))
    }

    @Test
    fun `T-UI-3 buildDialogTitle returns generic title for unknown type`() {
        assertEquals("Approval Required: custom_type", buildDialogTitle("custom_type"))
    }

    // ─── T-UI-4 ~ T-UI-6 : approvalDecisionsForType ───────────────────────────

    @Test
    fun `T-UI-4 approvalDecisionsForType strategy returns 3 decisions`() {
        val decisions = approvalDecisionsForType("strategy")
        assertEquals(3, decisions.size)
        assertEquals(listOf("split_execute", "no_split", "cancel"), decisions.map { it.value })
    }

    @Test
    fun `T-UI-5 approvalDecisionsForType supreme_court returns 3 decisions`() {
        val decisions = approvalDecisionsForType("supreme_court")
        assertEquals(3, decisions.size)
        assertEquals(listOf("uphold", "overturn", "redesign"), decisions.map { it.value })
    }

    @Test
    fun `T-UI-6 approvalDecisionsForType other returns 2 decisions`() {
        val decisions = approvalDecisionsForType("other")
        assertEquals(2, decisions.size)
        assertEquals(listOf("approve", "reject"), decisions.map { it.value })
    }

    // ─── T-UI-7 ~ T-UI-9 : isTimedOut ──────────────────────────────────────────

    @Test
    fun `T-UI-7 isTimedOut is true when remainingTimeSec is 0`() {
        val state =
            ApprovalModalState(
                pendingApproval = sampleApproval(),
                remainingTimeSec = 0,
            )
        assertTrue(state.isTimedOut)
    }

    @Test
    fun `T-UI-8 isTimedOut is false when remainingTimeSec is positive`() {
        val state =
            ApprovalModalState(
                pendingApproval = sampleApproval(),
                remainingTimeSec = 10,
            )
        assertFalse(state.isTimedOut)
    }

    @Test
    fun `T-UI-9 isTimedOut is false when remainingTimeSec is null`() {
        val state = ApprovalModalState(remainingTimeSec = null)
        assertFalse(state.isTimedOut)
    }

    // ─── T-UI-10 ~ T-UI-11b : formatCountdownText and calculateProgress ───────

    @Test
    fun `T-UI-10 formatCountdownText formats remaining time as m-ss`() {
        assertEquals("3:05 remaining", formatCountdownText(185, false))
    }

    @Test
    fun `T-UI-11a formatCountdownText returns Timed out when isTimedOut is true`() {
        assertEquals("Timed out", formatCountdownText(0, true))
    }

    @Test
    fun `T-UI-11b calculateProgress returns 0f when remaining is 0`() {
        assertEquals(0f, calculateProgress(0, 60))
    }

    @Test
    fun `T-UI-12 calculateProgress returns half when halfway remaining`() {
        assertEquals(0.5f, calculateProgress(30, 60))
    }

    // ─── T-UI-13 ~ T-UI-14 : isSubmitting ─────────────────────────────────────

    @Test
    fun `T-UI-13 isSubmitting reflects constructor value true`() {
        val state = ApprovalModalState(isSubmitting = true)
        assertTrue(state.isSubmitting)
    }

    @Test
    fun `T-UI-14 isSubmitting defaults to false`() {
        val state = ApprovalModalState()
        assertFalse(state.isSubmitting)
    }

    // ─── T-UI-15 ~ T-UI-16 : error ────────────────────────────────────────────

    @Test
    fun `T-UI-15 error field preserves value`() {
        val state = ApprovalModalState(error = "Network error")
        assertEquals("Network error", state.error)
    }

    @Test
    fun `T-UI-16 error defaults to null`() {
        val state = ApprovalModalState()
        assertNull(state.error)
    }

    // ─── T-UI-17 ~ T-UI-19 : ApprovalRequest.context ──────────────────────────

    @Test
    fun `T-UI-17 ApprovalRequest context exposes eta splitProposal detail`() {
        val request =
            ApprovalRequest(
                approvalType = "strategy",
                options = emptyList(),
                context =
                    ApprovalContext(
                        eta = "5 min",
                        splitProposal = "split into 2",
                        detail = "extra detail",
                    ),
            )
        val context = request.context
        assertNotNull(context)
        assertEquals("5 min", context.eta)
        assertEquals("split into 2", context.splitProposal)
        assertEquals("extra detail", context.detail)
    }

    @Test
    fun `T-UI-18 ApprovalRequest context with only eta has other fields null`() {
        val request =
            ApprovalRequest(
                approvalType = "strategy",
                options = emptyList(),
                context = ApprovalContext(eta = "3 min"),
            )
        val context = request.context
        assertNotNull(context)
        assertEquals("3 min", context.eta)
        assertNull(context.splitProposal)
        assertNull(context.detail)
    }

    @Test
    fun `T-UI-19 ApprovalRequest with null context`() {
        val request =
            ApprovalRequest(
                approvalType = "strategy",
                options = emptyList(),
                context = null,
            )
        assertNull(request.context)
    }

    // ─── Additional sanity checks referencing decision types (compile guard) ──

    @Test
    fun `sanity - decision sealed types are referenced`() {
        val strategy: com.orchestradashboard.shared.domain.model.ApprovalDecision = StrategyDecision.SplitExecute
        val court: com.orchestradashboard.shared.domain.model.ApprovalDecision = SupremeCourtDecision.Uphold
        val generic: com.orchestradashboard.shared.domain.model.ApprovalDecision = GenericDecision("approve")
        assertEquals("split_execute", strategy.value)
        assertEquals("uphold", court.value)
        assertEquals("approve", generic.value)
    }
}
