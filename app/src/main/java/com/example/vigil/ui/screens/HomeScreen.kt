package com.example.vigil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.vigil.detection.DetectionLog
import com.example.vigil.detection.DetectionLogEntry
import com.example.vigil.detection.MlLabel
import com.example.vigil.detection.Severity
import com.example.vigil.detection.severityColors
import com.example.vigil.ui.theme.VigilPrimary
import com.example.vigil.ui.theme.VigilPrimaryFixed
import com.example.vigil.ui.theme.VigilTheme

private const val COLLAPSED_LOG_COUNT = 8

/** Home tab */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    permissionGranted: Boolean = true,
    permanentlyDenied: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onOpenAppSettings: () -> Unit = {},
    overlayGranted: Boolean = true,
    usageGranted: Boolean = true,
    onEnableOverlay: () -> Unit = {},
    onEnableUsage: () -> Unit = {},
    onViewAll: () -> Unit = {},
    onEntryClick: (DetectionLogEntry) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // SMS off = detection can't run at all. Overlay off = detection runs and logs, but no warning popup is shown.
    val warningsOff = permissionGranted && !overlayGranted
    val amber = severityColors(Severity.MEDIUM, isSystemInDarkTheme())
    val statusTint = when {
        !permissionGranted -> MaterialTheme.colorScheme.error
        warningsOff -> amber.accent
        else -> VigilPrimary
    }
    val haloTint = when {
        !permissionGranted -> MaterialTheme.colorScheme.error
        warningsOff -> amber.iconBackground
        else -> VigilPrimaryFixed
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) { DetectionLog.ensureLoaded(context) }
    val logEntries by DetectionLog.entries.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        HomeTopBar(onSettingsClick = onSettingsClick)
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
                    Icon(
                        if (warningsOff) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = null, tint = statusTint, modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            when {
                !permissionGranted -> "Detection is not ready"
                warningsOff -> "Detection is on, warnings are off"
                else -> "Detection is ready"
            },
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                warningsOff -> "Vigil still scans your texts and saves threats to Logs, but it can't pop up a warning until Display over other apps is on."
                permissionGranted -> "Vigil AI is actively monitoring your texts for potential threats."
                permanentlyDenied -> "SMS permission was denied. Enable it in system settings to resume monitoring."
                else -> "Vigil AI needs SMS permission to monitor your device for potential threats."
            },
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth()
        )

        if (!permissionGranted) {
            Spacer(Modifier.height(20.dp))
            if (permanentlyDenied) {
                VigilPrimaryButton(text = "Open App Settings", onClick = onOpenAppSettings, showArrow = false)
            } else {
                VigilPrimaryButton(text = "Allow Permissions", onClick = onRequestPermission, showArrow = false)
            }
        }

        if (permissionGranted && (!overlayGranted || !usageGranted)) {
            Spacer(Modifier.height(20.dp))
            PermissionBanner(
                overlayMissing = !overlayGranted,
                usageMissing = !usageGranted,
                onEnableOverlay = onEnableOverlay,
                onEnableUsage = onEnableUsage,
            )
        }

        Spacer(Modifier.height(40.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Logs", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (logEntries.isNotEmpty()) {
                Text(
                    "View All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VigilPrimary,
                    modifier = Modifier.clickable(onClick = onViewAll)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (logEntries.isEmpty()) {
            Text(
                "No threats detected yet.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            logEntries.take(COLLAPSED_LOG_COUNT).forEach { entry ->
                LogRow(entry = entry, onClick = { onEntryClick(entry) })
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

internal fun DetectionLogEntry.title(): String = when (label) {
    MlLabel.SAFE -> "Message scanned"
    MlLabel.SCAM -> "Possible scam"
    MlLabel.HARASSMENT -> "Possible harassment"
}

internal fun DetectionLogEntry.severity(): Severity = when {
    label == MlLabel.SAFE -> Severity.SAFE
    confidence >= 0.85f -> Severity.HIGH
    else -> Severity.MEDIUM
}

internal fun DetectionLogEntry.toAnalysisArgs(): AnalysisArgs = AnalysisArgs(
    severity = severity(),
    verdict = title(),
    riskScore = (confidence * 100).toInt(),
    body = snippet,
)

internal fun DetectionLogEntry.relativeTime(): String =
    DateUtils.getRelativeTimeSpanString(
        timestampMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

@Composable
private fun HomeTopBar(onSettingsClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onSettingsClick)
        )
    }
}

/** Home banner for the optional permissions: overlay (no popup without it) and usage access (when the popup appears). */
@Composable
private fun PermissionBanner(
    overlayMissing: Boolean,
    usageMissing: Boolean,
    onEnableOverlay: () -> Unit,
    onEnableUsage: () -> Unit,
) {
    val colors = severityColors(Severity.MEDIUM, isSystemInDarkTheme())
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.iconBackground, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        if (overlayMissing) {
            BannerItem(
                title = "Warning popups are off",
                body = "Turn on Display over other apps so Vigil can warn you the moment a harmful message arrives.",
                accent = colors.accent,
                onClick = onEnableOverlay,
            )
        }
        if (overlayMissing && usageMissing) Spacer(Modifier.height(12.dp))
        if (usageMissing) {
            BannerItem(
                title = "Usage access is off",
                body = "Optional. With it, Vigil waits and shows the warning when you open the message, instead of popping up over whatever app you are in.",
                accent = colors.accent,
                onClick = onEnableUsage,
            )
        }
    }
}

@Composable
private fun BannerItem(title: String, body: String, accent: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(body, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
            Text("Turn on", color = accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun LogRow(entry: DetectionLogEntry, onClick: () -> Unit = {}) {
    val severity = entry.severity()
    val colors = severityColors(severity, isSystemInDarkTheme())
    VigilCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(colors.iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${(entry.confidence * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.title(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(6.dp))
                    Text(entry.relativeTime(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(entry.snippet, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
