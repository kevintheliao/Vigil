package com.example.vigil.detection

import org.junit.Assert.*
import org.junit.Test

class MessageScorerTest {

    @Test
    fun blankMessage_isUnknown() {
        val result = MessageScorer.score("")
        assertEquals(Severity.UNKNOWN, result.severity)
    }

    @Test
    fun ordinaryMessage_isSafe() {
        val result = MessageScorer.score("Hey, are we still on for dinner tonight?")
        assertEquals(Severity.SAFE, result.severity)
        assertNull(result.category)
    }

    @Test
    fun giftCardPlusUrgency_isScamAndHighRisk() {
        val result = MessageScorer.score(
            "Your account will be suspended! Act now and send a gift card to verify.",
        )
        assertEquals(ThreatCategory.SCAM, result.category)
        assertEquals(Severity.HIGH, result.severity)
    }

    @Test
    fun threatOfViolence_isHarassmentAndHighRisk() {
        val result = MessageScorer.score("I'll find you and hurt you, you should disappear.")
        assertEquals(ThreatCategory.HARASSMENT, result.category)
        assertEquals(Severity.HIGH, result.severity)
    }

    @Test
    fun shortenedLinkAlone_isMediumRisk() {
        val result = MessageScorer.score("Check this out https://bit.ly/abc123")
        assertEquals(Severity.MEDIUM, result.severity)
    }
}
