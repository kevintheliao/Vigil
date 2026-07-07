package com.example.vigil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.detection.DetectionIndicatorChip
import com.example.vigil.detection.DetectionUiState
import com.example.vigil.detection.Severity
import com.example.vigil.ui.theme.VigilTheme

/**
 * Onboarding step explaining the "Display over other apps" (SYSTEM_ALERT_WINDOW)
 * permission: Vigil needs it to float the detection chip on top of the user's
 * messaging app the moment a threat is found.
 */
@Composable
fun OverlayPermissionScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Live sample of the alert chip on a faux conversation backdrop, so the
        // user sees exactly what this permission enables.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            DetectionIndicatorChip(
                state = DetectionUiState(
                    severity = Severity.HIGH,
                    message = "High risk",
                    riskScore = 87
                ),
                onTap = {}
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Alerts Where You Need Them",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "When Vigil spots a dangerous message, it shows a small alert right on top of your messaging app, so you know before you reply. Android calls this permission “Display over other apps”.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureRow(
                Icons.Filled.Layers,
                "Instant Warnings",
                "A small chip appears over your messages the moment a scam or threat is detected."
            )
            FeatureRow(
                Icons.Filled.TouchApp,
                "Tap for Details",
                "Tap the alert to open Vigil's full analysis of why the message was flagged."
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun OverlayPermissionPreview() {
    VigilTheme { OverlayPermissionScreen() }
}
