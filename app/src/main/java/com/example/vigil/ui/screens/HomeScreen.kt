package com.example.vigil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import android.text.format.DateUtils
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.detection.DetectionLog
import com.example.vigil.detection.DetectionLogEntry
import com.example.vigil.detection.MlLabel
import com.example.vigil.ui.theme.VigilPrimary
import com.example.vigil.ui.theme.VigilPrimaryFixed
import com.example.vigil.ui.theme.VigilTheme

/** Home tab — detection status + recent logs. */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    permissionGranted: Boolean = true,
    onRequestPermission: () -> Unit = {}
) {
    val statusTint = if (permissionGranted) VigilPrimary else MaterialTheme.colorScheme.error
    val haloTint = if (permissionGranted) VigilPrimaryFixed else MaterialTheme.colorScheme.error
    val context = LocalContext.current
    LaunchedEffect(Unit) { DetectionLog.ensureLoaded(context) }
    val logEntries by DetectionLog.entries.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        HomeTopBar()
        Spacer(Modifier.height(24.dp))

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(200.dp).background(haloTint.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(96.dp).background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = statusTint, modifier = Modifier.size(48.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            if (permissionGranted) "Detection is ready" else "Detection is not ready",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (permissionGranted) {
                "Vigil AI is actively monitoring your device for potential threats."
            } else {
                "Vigil AI needs SMS permission to monitor your device for potential threats."
            },
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth()
        )

        if (!permissionGranted) {
            Spacer(Modifier.height(20.dp))
            VigilPrimaryButton(text = "Allow Permissions", onClick = onRequestPermission, showArrow = false)
        }

        Spacer(Modifier.height(40.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Logs", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("View All", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = VigilPrimary)
        }
        Spacer(Modifier.height(12.dp))
        if (logEntries.isEmpty()) {
            Text(
                "No threats detected yet.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            logEntries.forEach { entry ->
                LogRow(
                    icon = if (entry.label == MlLabel.SAFE) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    title = entry.title(),
                    time = entry.relativeTime(),
                    subtitle = entry.snippet
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun DetectionLogEntry.title(): String = when (label) {
    MlLabel.SAFE -> "Message scanned"
    MlLabel.SCAM -> "Possible scam"
    MlLabel.HARASSMENT -> "Possible harassment"
}

private fun DetectionLogEntry.relativeTime(): String =
    DateUtils.getRelativeTimeSpanString(
        timestampMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

@Composable
private fun HomeTopBar() {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VigilWordmark()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun LogRow(icon: ImageVector, title: String, subtitle: String, time: String? = null) {
    VigilCard {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    if (time != null) {
                        Spacer(Modifier.width(6.dp))
                        Text(time, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun HomePreview() {
    VigilTheme { HomeScreen() }
}
