package com.example.vigil.detection

import android.content.Context

/**
 * User-tunable false-positive / false-negative tradeoff.
 *
 * The threshold is the minimum "threat vs safe" confidence needed to flag a message. 0.5 matches the
 * model's plain argmax, lower flags more (more false positives), higher flags less (more false negatives).
 */
object DetectionSensitivity {
    const val DEFAULT_THRESHOLD = 0.5f
    const val MIN_THRESHOLD = 0.2f
    const val MAX_THRESHOLD = 0.8f

    private const val PREFS_NAME = "vigil_prefs"
    private const val KEY_THRESHOLD = "detection_threshold"

    fun threshold(context: Context): Float =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_THRESHOLD, DEFAULT_THRESHOLD)
            .coerceIn(MIN_THRESHOLD, MAX_THRESHOLD)

    fun setThreshold(context: Context, threshold: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_THRESHOLD, threshold.coerceIn(MIN_THRESHOLD, MAX_THRESHOLD))
            .apply()
    }

    /** Multiplier for [MessageScorer]'s severity cutoffs so the keyword scorer follows the same setting. */
    fun scorerScale(threshold: Float): Float = threshold / DEFAULT_THRESHOLD
}
