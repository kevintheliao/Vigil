package com.example.vigil.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val previewStates = listOf(
    DetectionUiState(Severity.SAFE, "Message looks safe"),
    DetectionUiState(Severity.MEDIUM, "Potentially manipulative", riskScore = 65),
    DetectionUiState(Severity.HIGH, "High risk: Scam", riskScore = 87),
    DetectionUiState(Severity.UNKNOWN, "Analyzing message"),
)

@Composable
private fun AllStates(darkTheme: Boolean) {
    Column(
        modifier = Modifier
            .background(if (darkTheme) Color(0xFF101214) else Color(0xFFF2F4F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        previewStates.forEach { state ->
            DetectionIndicatorChip(state = state, onTap = {}, darkTheme = darkTheme)
        }
    }
}

@Preview(name = "All states – light", showBackground = true)
@Composable
private fun DetectionIndicatorLightPreview() {
    AllStates(darkTheme = false)
}

@Preview(name = "All states – dark", showBackground = true, backgroundColor = 0xFF101214)
@Composable
private fun DetectionIndicatorDarkPreview() {
    AllStates(darkTheme = true)
}

@Preview(name = "Safe", showBackground = true)
@Composable
private fun SafePreview() {
    DetectionIndicatorChip(state = previewStates[0], onTap = {}, darkTheme = false)
}

@Preview(name = "Medium risk", showBackground = true)
@Composable
private fun MediumRiskPreview() {
    DetectionIndicatorChip(state = previewStates[1], onTap = {}, darkTheme = false)
}

@Preview(name = "High risk", showBackground = true)
@Composable
private fun HighRiskPreview() {
    DetectionIndicatorChip(state = previewStates[2], onTap = {}, darkTheme = false)
}
