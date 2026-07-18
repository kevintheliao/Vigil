package com.example.vigil.detection

/** Severity of a detected message, used to color the overlay chip. */
enum class Severity {
    SAFE, MEDIUM, HIGH, UNKNOWN
}

/** UI state for the detection chip shown as a system overlay over other apps. */
data class DetectionUiState(
    val severity: Severity,
    /** Short text to show, e.g. "High risk: Scam". */
    val message: String,
    /** Optional risk score 0-100 shown after the message. */
    val riskScore: Int? = null,
    /** Full SMS text; not rendered by the chip, carried through to the analysis screen. */
    val body: String = "",
    val isVisible: Boolean = true,
)
