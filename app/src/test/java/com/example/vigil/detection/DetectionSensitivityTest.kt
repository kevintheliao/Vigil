package com.example.vigil.detection

import org.junit.Assert.*
import org.junit.Test

class DetectionSensitivityTest {

    private val default = DetectionSensitivity.DEFAULT_THRESHOLD

    // probs are [safe, scam, harassment]
    @Test
    fun defaultThreshold_matchesArgmax() {
        val cases = listOf(
            floatArrayOf(0.90f, 0.06f, 0.04f),
            floatArrayOf(0.20f, 0.60f, 0.20f),
            floatArrayOf(0.10f, 0.20f, 0.70f),
            floatArrayOf(0.40f, 0.35f, 0.25f), // safe wins the argmax
            floatArrayOf(0.34f, 0.36f, 0.30f), // scam wins the argmax narrowly
        )
        for (p in cases) {
            val argmax = p.indices.maxByOrNull { p[it] }!!
            assertEquals(MlLabel.values()[argmax], decide(p, default).label)
        }
    }

    @Test
    fun lowerThreshold_flagsBorderlineMessage() {
        val borderline = floatArrayOf(0.45f, 0.30f, 0.25f) // argmax says safe
        assertEquals(MlLabel.SAFE, decide(borderline, default).label)
        assertEquals(MlLabel.SCAM, decide(borderline, 0.3f).label)
    }

    @Test
    fun higherThreshold_suppressesWeakFlag() {
        val weak = floatArrayOf(0.40f, 0.45f, 0.15f) // argmax says scam
        assertEquals(MlLabel.SCAM, decide(weak, default).label)
        assertEquals(MlLabel.SAFE, decide(weak, 0.7f).label)
    }

    @Test
    fun confidentThreat_stillFlaggedAtStrictestSetting() {
        val strong = floatArrayOf(0.02f, 0.03f, 0.95f)
        assertEquals(MlLabel.HARASSMENT, decide(strong, DetectionSensitivity.MAX_THRESHOLD).label)
    }

    @Test
    fun scorer_defaultScaleUnchanged() {
        val text = "Check this out https://bit.ly/abc123"
        assertEquals(Severity.MEDIUM, MessageScorer.score(text, DetectionSensitivity.scorerScale(default)).severity)
    }

    @Test
    fun scorer_lowSensitivity_dropsMediumToSafe() {
        val text = "Check this out https://bit.ly/abc123" // 30 points
        val strict = DetectionSensitivity.scorerScale(DetectionSensitivity.MAX_THRESHOLD)
        assertEquals(Severity.SAFE, MessageScorer.score(text, strict).severity)
    }

    @Test
    fun scorer_highSensitivity_raisesSeverity() {
        val text = "Act now to verify your account" // 15 points, urgency only
        assertEquals(Severity.SAFE, MessageScorer.score(text).severity)
        val loose = DetectionSensitivity.scorerScale(DetectionSensitivity.MIN_THRESHOLD)
        assertNotEquals(Severity.SAFE, MessageScorer.score(text, loose).severity)
    }
}
