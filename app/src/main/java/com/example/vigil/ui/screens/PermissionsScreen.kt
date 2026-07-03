package com.example.vigil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
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
import com.example.vigil.ui.theme.VigilTheme

/** Permission grant — "Enable Protection". */
@Composable
fun PermissionsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VigilWordmark(Modifier.align(Alignment.Start).padding(top = 8.dp))
        Spacer(Modifier.weight(1f))
        ShieldEmblem(size = 170)
        Spacer(Modifier.height(32.dp))
        Text(
            "Enable Protection",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "To detect threats in real-time, Vigil needs permission to monitor your incoming messages. Your data remains private and on-device.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureRow(Icons.Filled.Lock, "Privacy First", "AI processing happens entirely on your phone. No messages ever leave your device.")
            FeatureRow(Icons.Filled.DateRange, "Real-time Analysis", "Instant alerts when a malicious link or phishing attempt is detected in a text.")
        }

        Spacer(Modifier.weight(1f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun PermissionsPreview() {
    VigilTheme { PermissionsScreen() }
}
