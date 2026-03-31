package com.orchestradashboard.shared.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.MonitoredStep
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

private const val TIMER_INTERVAL_MS = 1000L

@Composable
fun StepNode(
    step: MonitoredStep,
    modifier: Modifier = Modifier,
) {
    val color = stepStatusColor(step.status)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StepCircle(step.status, color)

        Text(
            text = step.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )

        StepTimer(step)
    }
}

@Composable
private fun StepCircle(
    status: StepStatus,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (status == StepStatus.RUNNING) {
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        )
        Box(
            modifier = modifier.size(24.dp).alpha(alpha).background(color, CircleShape),
        )
    } else {
        Box(
            modifier = modifier.size(24.dp).background(color, CircleShape),
        )
    }
}

@Composable
private fun StepTimer(
    step: MonitoredStep,
    modifier: Modifier = Modifier,
) {
    if (step.status == StepStatus.RUNNING && step.startedAtMs != null) {
        var elapsedMs by remember(step.startedAtMs) {
            mutableLongStateOf(Clock.System.now().toEpochMilliseconds() - step.startedAtMs)
        }
        LaunchedEffect(step.startedAtMs) {
            while (true) {
                delay(TIMER_INTERVAL_MS)
                elapsedMs = Clock.System.now().toEpochMilliseconds() - step.startedAtMs
            }
        }
        Text(
            text = formatElapsedMs(elapsedMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )
    } else if (step.elapsedMs > 0) {
        Text(
            text = formatElapsedMs(step.elapsedMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    }
}

internal fun stepStatusColor(status: StepStatus): Color =
    when (status) {
        StepStatus.PENDING -> Color(0xFF9E9E9E)
        StepStatus.RUNNING -> Color(0xFF2196F3)
        StepStatus.PASSED -> Color(0xFF4CAF50)
        StepStatus.FAILED -> Color(0xFFF44336)
        StepStatus.SKIPPED -> Color(0xFFFF9800)
    }

internal fun formatElapsedMs(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    return "${m}m ${s}s"
}
