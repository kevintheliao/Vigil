package com.example.vigil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
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
import com.example.vigil.ui.theme.VigilPrimary
import com.example.vigil.ui.theme.VigilPrimaryFixed
import com.example.vigil.ui.theme.VigilTheme

/**
 * Onboarding step explaining the "Usage access" (PACKAGE_USAGE_STATS)
 * permission: it lets Vigil keep its alert chip inside the messaging app
 * instead of popping up over whatever the user happens to be doing.
 * Optional — skipping just means alerts can appear over any app.
 */
@Composable
fun UsageAccessScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(96.dp).background(VigilPrimaryFixed.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.NotificationsPaused,
                contentDescription = null,
                tint = VigilPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Alerts Only While Texting",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Keep Vigil's alerts inside your messaging app, so they never interrupt a game or video. Android calls this permission “Usage access”.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureRow(
                Icons.Filled.Sms,
                "No Interruptions",
                "Alert chips appear over your messaging app and nowhere else."
            )
            FeatureRow(
                Icons.Filled.Lock,
                "Private by Design",
                "Vigil only checks which app is open when a text arrives. Nothing is tracked or stored, and Vigil has no internet access."
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun UsageAccessPreview() {
    VigilTheme { UsageAccessScreen() }
}
