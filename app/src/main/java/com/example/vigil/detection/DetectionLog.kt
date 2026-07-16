package com.example.vigil.detection

import android.content.Context
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
    private const val PREFS_NAME = "vigil_prefs"
    private const val KEY_ENTRIES = "detection_log_entries"
    private const val MAX_ENTRIES = 20

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
            snippet = messageBody.take(60),
            confidence = result.confidence,
            timestampMillis = System.currentTimeMillis()
        )
        val updated = (listOf(entry) + _entries.value).take(MAX_ENTRIES)
        _entries.value = updated
        writePrefs(context, updated)
    }

    private fun readPrefs(context: Context): List<DetectionLogEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ENTRIES, null) ?: return emptyList()
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
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENTRIES, array.toString())
            .apply()
    }
}
