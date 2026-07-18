package com.example.vigil.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlin.concurrent.thread

/** Classifies every incoming SMS on a background thread (goAsync) and shows the chip for non-SAFE results. */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val body = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            .joinToString(separator = "") { it.messageBody ?: "" }
        if (body.isBlank()) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()
        thread {
            try {
                val result = classifier(appContext).classify(body)
                if (result.label != MlLabel.SAFE) {
                    DetectionLog.add(appContext, result, body)
                    DetectionOverlayService.show(appContext, result.toDetectionUiState(body))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun MlClassification.toDetectionUiState(body: String): DetectionUiState {
        val severity = if (confidence >= 0.85f) Severity.HIGH else Severity.MEDIUM
        val message = if (label == MlLabel.SCAM) "Possible scam" else "Possible harassment"
        return DetectionUiState(severity = severity, message = message, riskScore = (confidence * 100).toInt(), body = body)
    }

    companion object {
        @Volatile private var instance: OnnxMessageClassifier? = null

        // loaded once and reused across messages - reloading a 67MB model per text would be wasteful
        private fun classifier(context: Context): OnnxMessageClassifier =
            instance ?: synchronized(this) {
                instance ?: OnnxMessageClassifier(context).also { instance = it }
            }
    }
}
