package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.AgentEvent

@Composable
fun EventFeed(
    events: List<AgentEvent>,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No events recorded.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val listState = rememberLazyListState()

        // Only auto-scroll if the user is already near the top
        LaunchedEffect(events.firstOrNull()?.id) {
            if (events.isNotEmpty() && listState.firstVisibleItemIndex < 2) {
                listState.animateScrollToItem(0)
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(events, key = { it.id }) { event ->
                EventItem(event)
            }
        }
    }
}
