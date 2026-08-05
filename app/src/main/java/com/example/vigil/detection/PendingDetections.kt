package com.example.vigil.detection

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray
import org.json.JSONObject

object PendingDetections {
    private const val PREFS_NAME = "vigil_pending_detections_secure"
    private const val KEY_ENTRIES = "pending_entries"
    private const val JOB_ID = 4200
    private const val QUERY_LOOKBACK_MILLIS = 30_000L

    private data class Pending(val sender: String, val body: String, val state: DetectionUiState, val sinceMillis: Long)

    @Synchronized
    fun add(context: Context, sender: String, body: String, state: DetectionUiState) {
        val appContext = context.applicationContext
        val list = load(appContext) + Pending(sender, body, state, System.currentTimeMillis())
        save(appContext, list)
        scheduleJob(appContext)
    }

    @Synchronized
    fun checkPending(context: Context, onDone: () -> Unit = {}) {
        val appContext = context.applicationContext
        var list = load(appContext)
        if (list.isEmpty()) {
            onDone()
            return
        }
        val cutoff = list.minOf { it.sinceMillis } - QUERY_LOOKBACK_MILLIS
        val matches = mutableListOf<Pending>()
        appContext.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.READ),
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(cutoff.toString()),
            null,
        )?.use { cursor ->
            val addressIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val readIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)
            while (cursor.moveToNext() && list.isNotEmpty()) {
                if (cursor.getInt(readIdx) != 1) continue
                val match = list.firstOrNull {
                    it.body == cursor.getString(bodyIdx) &&
                        PhoneNumberUtils.compare(it.sender, cursor.getString(addressIdx))
                } ?: continue
                list = list - match
                matches += match
            }
        }
        save(appContext, list)
        if (list.isNotEmpty()) scheduleJob(appContext)

        if (matches.isEmpty()) {
            onDone()
            return
        }
        val remaining = java.util.concurrent.atomic.AtomicInteger(matches.size)
        matches.forEach { match ->
            DetectionOverlayService.show(appContext, match.state) {
                if (remaining.decrementAndGet() == 0) onDone()
            }
        }
    }

    private fun scheduleJob(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val job = JobInfo.Builder(JOB_ID, ComponentName(context, PendingDetectionJobService::class.java))
            .addTriggerContentUri(
                JobInfo.TriggerContentUri(Telephony.Sms.CONTENT_URI, JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS)
            )
            .setTriggerContentMaxDelay(0)
            .build()
        scheduler.schedule(job)
    }

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun load(context: Context): List<Pending> {
        val raw = prefs(context).getString(KEY_ENTRIES, null) ?: return emptyList()
        val array = JSONArray(raw)
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            Pending(
                sender = o.getString("sender"),
                body = o.getString("body"),
                state = DetectionUiState(
                    severity = Severity.valueOf(o.getString("severity")),
                    message = o.getString("message"),
                    riskScore = if (o.has("riskScore")) o.getInt("riskScore") else null,
                    body = o.getString("body"),
                ),
                sinceMillis = o.getLong("sinceMillis"),
            )
        }
    }

    private fun save(context: Context, list: List<Pending>) {
        val array = JSONArray()
        list.forEach { p ->
            array.put(
                JSONObject().apply {
                    put("sender", p.sender)
                    put("body", p.body)
                    put("severity", p.state.severity.name)
                    put("message", p.state.message)
                    p.state.riskScore?.let { put("riskScore", it) }
                    put("sinceMillis", p.sinceMillis)
                }
            )
        }
        prefs(context).edit().putString(KEY_ENTRIES, array.toString()).apply()
    }
}
