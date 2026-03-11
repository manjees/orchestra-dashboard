package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.domain.model.AgentEvent

@Composable
fun EventFeed(
    events: List<AgentEvent>,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No events yet.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val sortedEvents = events.sortedByDescending { it.timestamp }
    val listState = rememberLazyListState()

    // Auto-scroll to top (latest event) only when new events arrive and user is near the top
    LaunchedEffect(sortedEvents.firstOrNull()?.id) {
        if (listState.firstVisibleItemIndex <= 1) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        items(sortedEvents, key = { it.id }) { event ->
            EventItem(event = event)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}
