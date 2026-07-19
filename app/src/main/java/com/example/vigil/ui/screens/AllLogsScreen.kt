package com.example.vigil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.detection.DetectionLog
import com.example.vigil.detection.DetectionLogEntry

/** All Logs tab — full detection history, newest first. */
@Composable
fun AllLogsScreen(modifier: Modifier = Modifier, onEntryClick: (DetectionLogEntry) -> Unit = {}) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { DetectionLog.ensureLoaded(context) }
    val logEntries by DetectionLog.entries.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "All Logs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (logEntries.isNotEmpty()) {
                    TextButton(onClick = { DetectionLog.clear(context) }) {
                        Text("Clear history", fontSize = 13.sp)
                    }
                }
            }
        }
        if (logEntries.isEmpty()) {
            item {
                Text(
                    "No threats detected yet.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(logEntries) { entry -> LogRow(entry = entry, onClick = { onEntryClick(entry) }) }
        }
    }
}
