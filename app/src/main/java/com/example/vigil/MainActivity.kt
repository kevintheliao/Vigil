package com.example.vigil

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.vigil.detection.DetectionOverlayService
import com.example.vigil.detection.Severity
import com.example.vigil.ui.screens.AnalysisArgs
import com.example.vigil.ui.screens.VigilApp
import com.example.vigil.ui.theme.VigilTheme

class MainActivity : ComponentActivity() {

    private val analysisArgs = mutableStateOf<AnalysisArgs?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        analysisArgs.value = intent.toAnalysisArgs()

        setContent {
            VigilTheme {
                VigilApp(
                    modifier = Modifier.fillMaxSize(),
                    analysisArgs = analysisArgs.value,
                    onAnalysisDismissed = { analysisArgs.value = null },
                )
            }
        }
    }

    //chip taps while the activity is alive arrive here (SINGLE_TOP)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.toAnalysisArgs()?.let { analysisArgs.value = it }
    }

    private fun Intent.toAnalysisArgs(): AnalysisArgs? {
        if (!getBooleanExtra(DetectionOverlayService.EXTRA_OPEN_ANALYSIS, false)) return null
        return AnalysisArgs(
            severity = getStringExtra(DetectionOverlayService.EXTRA_SEVERITY)
                ?.let { runCatching { Severity.valueOf(it) }.getOrNull() }
                ?: Severity.UNKNOWN,
            verdict = getStringExtra(DetectionOverlayService.EXTRA_MESSAGE) ?: "Flagged message",
            riskScore = if (hasExtra(DetectionOverlayService.EXTRA_RISK_SCORE)) {
                getIntExtra(DetectionOverlayService.EXTRA_RISK_SCORE, 0)
            } else null,
            body = getStringExtra(DetectionOverlayService.EXTRA_BODY) ?: "",
        )
    }
}
