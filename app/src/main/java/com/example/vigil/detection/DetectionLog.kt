package com.example.vigil.detection

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class DetectionLogEntry(
    val label: MlLabel,
    val snippet: String,
    val confidence: Float,
    val timestampMillis: Long
)

/** Persists recent SMS classifications so Home can show real history instead of placeholders. */
object DetectionLog {
    // own file (not shared with OnboardingPrefs) since this one holds real SMS
    // snippets and needs Keystore-backed encryption at rest
    private const val PREFS_NAME = "vigil_detection_log_secure"
    private const val KEY_ENTRIES = "detection_log_entries"
    private const val MAX_ENTRIES = 100

    private val _entries = MutableStateFlow<List<DetectionLogEntry>>(emptyList())
    val entries: StateFlow<List<DetectionLogEntry>> = _entries.asStateFlow()

    @Volatile private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            _entries.value = readPrefs(context)
            loaded = true
        }
    }

    fun add(context: Context, result: MlClassification, messageBody: String) {
        ensureLoaded(context)
        val entry = DetectionLogEntry(
            label = result.label,
            snippet = messageBody.take(300),
            confidence = result.confidence,
            timestampMillis = System.currentTimeMillis()
        )
        val updated = (listOf(entry) + _entries.value).take(MAX_ENTRIES)
        _entries.value = updated
        writePrefs(context, updated)
    }

    fun clear(context: Context) {
        _entries.value = emptyList()
        writePrefs(context, emptyList())
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

    private fun readPrefs(context: Context): List<DetectionLogEntry> {
        val raw = prefs(context).getString(KEY_ENTRIES, null) ?: return emptyList()
        val array = JSONArray(raw)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            DetectionLogEntry(
                label = MlLabel.valueOf(obj.getString("label")),
                snippet = obj.getString("snippet"),
                confidence = obj.getDouble("confidence").toFloat(),
                timestampMillis = obj.getLong("timestamp")
            )
        }
    }

    private fun writePrefs(context: Context, entries: List<DetectionLogEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("label", entry.label.name)
                    .put("snippet", entry.snippet)
                    .put("confidence", entry.confidence.toDouble())
                    .put("timestamp", entry.timestampMillis)
            )
        }
        prefs(context)
            .edit()
            .putString(KEY_ENTRIES, array.toString())
            .apply()
    }
}
