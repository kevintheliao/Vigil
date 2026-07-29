package com.example.vigil.detection

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.PhoneNumberUtils

object PendingDetections {
    private data class Pending(val sender: String, val body: String, val state: DetectionUiState, val sinceMillis: Long)

    private val pending = mutableListOf<Pending>()
    private var observer: ContentObserver? = null

    @Synchronized
    fun add(context: Context, sender: String, body: String, state: DetectionUiState) {
        pending += Pending(sender, body, state, System.currentTimeMillis())
        ensureObserver(context.applicationContext)
    }

    private fun ensureObserver(context: Context) {
        if (observer != null) return
        val obs = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = checkPending(context)
        }
        observer = obs
        context.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, obs)
    }

    @Synchronized
    private fun checkPending(context: Context) {
        if (pending.isEmpty()) return
        val cutoff = pending.minOf { it.sinceMillis }
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.READ),
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(cutoff.toString()),
            null,
        )?.use { cursor ->
            val addressIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val readIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)
            while (cursor.moveToNext() && pending.isNotEmpty()) {
                if (cursor.getInt(readIdx) != 1) continue
                val match = pending.firstOrNull {
                    it.body == cursor.getString(bodyIdx) &&
                        PhoneNumberUtils.compare(it.sender, cursor.getString(addressIdx))
                } ?: continue
                pending.remove(match)
                DetectionOverlayService.show(context, match.state)
            }
        }
    }
}
