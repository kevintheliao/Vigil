package com.example.vigil.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.vigil.detection.DetectionLog
import com.example.vigil.detection.DetectionOverlayService
import com.example.vigil.ui.theme.VigilPrimary

private fun hasSmsPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED

/** Settings — permission status, clear log, privacy/about. Reached via the gear icon on Home. */
@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var smsGranted by remember { mutableStateOf(hasSmsPermission(context)) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var usageGranted by remember { mutableStateOf(DetectionOverlayService.hasUsageAccess(context)) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results -> smsGranted = results.values.all { it } }

    //shared by overlay + usage access: both just send the user to a system settings screen and back
    val systemSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        overlayGranted = Settings.canDrawOverlays(context)
        usageGranted = DetectionOverlayService.hasUsageAccess(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(16.dp))

        SectionLabel("Permissions")
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsRow(
                icon = Icons.Filled.Sms,
                title = "SMS access",
                granted = smsGranted,
                onClick = { smsPermissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)) },
            )
            SettingsRow(
                icon = Icons.Filled.Layers,
                title = "Display over other apps",
                granted = overlayGranted,
                onClick = {
                    systemSettingsLauncher.launch(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri())
                    )
                },
            )
            SettingsRow(
                icon = Icons.Filled.Visibility,
                title = "Usage access",
                granted = usageGranted,
                onClick = { systemSettingsLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel("Data")
        Spacer(Modifier.height(8.dp))
        FeatureRow(
            Icons.Filled.DeleteOutline,
            "Clear detection log",
            "Deletes every scanned message record stored on this device.",
            modifier = Modifier.clickable { showClearConfirm = true },
        )

        Spacer(Modifier.height(24.dp))
        SectionLabel("About")
        Spacer(Modifier.height(8.dp))
        FeatureRow(
            Icons.Filled.Lock,
            "Privacy",
            "Vigil processes every message on-device. Nothing is uploaded, logged remotely, or shared.",
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Version ${appVersionName(context)}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
        Spacer(Modifier.height(24.dp))
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear detection log?") },
            text = { Text("This deletes every scanned message record on this device. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    DetectionLog.clear(context)
                    showClearConfirm = false
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, granted: Boolean, onClick: () -> Unit) {
    VigilCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = VigilPrimary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text(
                if (granted) "Granted" else "Not granted",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (granted) VigilPrimary else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun appVersionName(context: android.content.Context): String =
    runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "unknown"
