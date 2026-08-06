package com.example.vigil.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.util.Log
import kotlin.concurrent.thread

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString(separator = "") { it.messageBody ?: "" }
        if (body.isBlank()) return
        val sender = messages.firstOrNull()?.originatingAddress

        val appContext = context.applicationContext
        val pendingResult = goAsync()
        thread {
            try {
                val normalized = TextNormalizer.normalize(body)
                val result = classifier(appContext).classify(normalized)
                val scored = MessageScorer.score(normalized)
                if (result.label != MlLabel.SAFE || scored.severity != Severity.SAFE) {
                    DetectionLog.add(appContext, result.orFallbackTo(scored), body)
                    val state = buildUiState(result, scored, body)
                    if (sender != null && DetectionOverlayService.hasUsageAccess(appContext) &&
                        !isThreadBeingViewed(appContext, sender, body)
                    ) {
                        PendingDetections.add(appContext, sender, body, state)
                    } else {
                        DetectionOverlayService.show(appContext, state)
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Failed to classify incoming message", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    // checks if the SMS app already marked this message read, meaning user is looking at it now
    private fun isThreadBeingViewed(context: Context, sender: String, body: String): Boolean {
        Thread.sleep(THREAD_VIEWED_CHECK_DELAY_MILLIS)
        val cutoff = System.currentTimeMillis() - THREAD_VIEWED_CHECK_WINDOW_MILLIS
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.READ),
            "${Telephony.Sms.DATE} > ?",
            arrayOf(cutoff.toString()),
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            val addressIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val readIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)
            while (cursor.moveToNext()) {
                val matchesBody = cursor.getString(bodyIdx) == body
                val matchesSender = PhoneNumberUtils.compare(cursor.getString(addressIdx), sender)
                if (matchesBody && matchesSender) return cursor.getInt(readIdx) == 1
            }
        }
        return false
    }

    // MessageScorer catches specific templates (romance scam, grooming, obfuscated keywords) the ML model wasn't trained on;
    // ML result still wins when it fired, since it's the higher-recall general classifier
    private fun MlClassification.orFallbackTo(scored: ScoreResult): MlClassification {
        if (label != MlLabel.SAFE) return this
        val fallbackLabel = if (scored.category == ThreatCategory.HARASSMENT || scored.category == ThreatCategory.GROOMING) {
            MlLabel.HARASSMENT
        } else {
            MlLabel.SCAM
        }
        return MlClassification(fallbackLabel, scored.riskScore / 100f)
    }

    private fun buildUiState(ml: MlClassification, scored: ScoreResult, body: String): DetectionUiState {
        val mlSeverity = when {
            ml.label == MlLabel.SAFE -> Severity.SAFE
            ml.confidence >= 0.85f -> Severity.HIGH
            else -> Severity.MEDIUM
        }
        val severity = if (scored.severity.rank() > mlSeverity.rank()) scored.severity else mlSeverity
        val message = when {
            scored.category == ThreatCategory.GROOMING -> "Possible grooming attempt"
            scored.category == ThreatCategory.ROMANCE_SCAM -> "Possible romance scam"
            ml.label == MlLabel.SCAM -> "Possible scam"
            ml.label == MlLabel.HARASSMENT -> "Possible harassment"
            scored.category == ThreatCategory.HARASSMENT -> "Possible harassment"
            else -> "Possible scam"
        }
        val riskScore = maxOf((ml.confidence * 100).toInt(), scored.riskScore)
        return DetectionUiState(severity = severity, message = message, riskScore = riskScore, body = body)
    }

    private fun Severity.rank() = when (this) {
        Severity.SAFE -> 0
        Severity.MEDIUM -> 1
        Severity.HIGH -> 2
        Severity.UNKNOWN -> -1
    }

    companion object {
        @Volatile private var instance: OnnxMessageClassifier? = null

        private const val THREAD_VIEWED_CHECK_DELAY_MILLIS = 700L
        private const val THREAD_VIEWED_CHECK_WINDOW_MILLIS = 10_000L

        // model is 67MB, don't reload per text
        private fun classifier(context: Context): OnnxMessageClassifier =
            instance ?: synchronized(this) {
                instance ?: OnnxMessageClassifier(context).also { instance = it }
            }
    }
}
