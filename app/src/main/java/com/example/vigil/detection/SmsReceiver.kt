package com.example.vigil.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import kotlin.concurrent.thread

// checks every text in the background and shows the chip if it's not safe
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
                val result = classifier(appContext).classify(body)
                if (result.label != MlLabel.SAFE) {
                    DetectionLog.add(appContext, result, body)
                    // skip chip only if user has Usage Access and is looking at this exact thread
                    val shouldShow = sender == null ||
                        !DetectionOverlayService.hasUsageAccess(appContext) ||
                        isThreadBeingViewed(appContext, sender, body)
                    if (shouldShow) {
                        DetectionOverlayService.show(appContext, result.toDetectionUiState(body))
                    }
                }
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

    private fun MlClassification.toDetectionUiState(body: String): DetectionUiState {
        val severity = if (confidence >= 0.85f) Severity.HIGH else Severity.MEDIUM
        val message = if (label == MlLabel.SCAM) "Possible scam" else "Possible harassment"
        return DetectionUiState(severity = severity, message = message, riskScore = (confidence * 100).toInt(), body = body)
    }

    companion object {
        @Volatile private var instance: OnnxMessageClassifier? = null

        private const val THREAD_VIEWED_CHECK_DELAY_MILLIS = 700L
        private const val THREAD_VIEWED_CHECK_WINDOW_MILLIS = 10_000L

        // load once and reuse, model is 67MB so don't reload per text
        private fun classifier(context: Context): OnnxMessageClassifier =
            instance ?: synchronized(this) {
                instance ?: OnnxMessageClassifier(context).also { instance = it }
            }
    }
}
