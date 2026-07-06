package com.example.vigil.detection

/**
 * Severity of a detected message, used to color the overlay chip.
 */
enum class Severity {
    SAFE, MEDIUM, HIGH, UNKNOWN
}

/**
 * UI state for the minimal detection indicator chip shown as a system
 * overlay while the user is in another app (e.g. Android Messages).
 */
data class DetectionUiState(
    val severity: Severity,
    /** Short text to show, e.g. "High risk: Scam". */
    val message: String,
    /** Optional risk score 0-100 shown after the message. */
    val riskScore: Int? = null,
    val isVisible: Boolean = true,
)
