package com.example.vigil.detection

/** What kind of harm a flagged message appears to be. */
enum class ThreatCategory { SCAM, HARASSMENT, ROMANCE_SCAM, GROOMING }

data class ScoreResult(
    val severity: Severity,
    val riskScore: Int,
    val category: ThreatCategory?,
    val matchedSignals: List<String>,
)

/** Keyword/regex scorer: matched signals add weight, total (0-100) buckets into a [Severity]. */
object MessageScorer {

    private data class Signal(
        val label: String,
        val category: ThreatCategory,
        val weight: Int,
        val pattern: Regex,
    )

    private val signals = listOf(
        // Scam: urgency / pressure
        Signal(
            "urgency language", ThreatCategory.SCAM, 15,
            Regex("(?i)act now|verify (immediately|now)|account (will be |has been )?(suspended|locked)|final notice|immediate action required"),
        ),
        // Scam: payment / credential requests
        Signal("gift card request", ThreatCategory.SCAM, 45, Regex("(?i)gift card|itunes card|google play card")),
        Signal(
            "wire/payment request", ThreatCategory.SCAM, 25,
            Regex("(?i)wire transfer|send money|bank account number|routing number"),
        ),
        Signal("otp/code request", ThreatCategory.SCAM, 25, Regex("(?i)verification code|one[- ]time (code|password)|\\botp\\b")),
        // Scam: links
        Signal(
            "shortened link", ThreatCategory.SCAM, 30,
            Regex("(?i)https?://(bit\\.ly|tinyurl\\.com|t\\.co|goo\\.gl|is\\.gd)/\\S+"),
        ),
        Signal(
            "brand + link", ThreatCategory.SCAM, 15,
            Regex("(?i)(amazon|paypal|bank|irs|usps|fedex|ups)\\b.{0,40}https?://"),
        ),
        // Scam: prize / refund hooks
        Signal(
            "prize/refund hook", ThreatCategory.SCAM, 20,
            Regex("(?i)you('| ha)ve won|unclaimed (package|refund|prize)|claim your (prize|refund)"),
        ),

        // Harassment: direct threats
        Signal(
            "threat of violence", ThreatCategory.HARASSMENT, 40,
            Regex("(?i)i('| a)?ll (kill|hurt|beat|find) you|watch your back|you('re| are) dead"),
        ),
        Signal(
            "self-harm encouragement", ThreatCategory.HARASSMENT, 45,
            Regex("(?i)kill yourself|\\bkys\\b|you should (die|disappear)"),
        ),
        Signal(
            "insults/degradation", ThreatCategory.HARASSMENT, 15,
            Regex("(?i)nobody (likes|wants) you|you('re| are) (worthless|pathetic|a loser|disgusting)"),
        ),
        Signal(
            "exclusion/isolation", ThreatCategory.HARASSMENT, 10,
            Regex("(?i)no one (cares|likes) about you|everyone hates you"),
        ),
        Signal(
            "doxxing threat", ThreatCategory.HARASSMENT, 20,
            Regex("(?i)i('ll| will) (post|share|leak) your (address|photos|number)"),
        ),
        Signal(
            "guilt-tripping language", ThreatCategory.HARASSMENT, 15,
            Regex("(?i)if you (really )?(loved|cared about) me you('d| would)|after everything i('ve| have) done for you"),
        ),
        Signal(
            "self-harm threat to control", ThreatCategory.HARASSMENT, 35,
            Regex("(?i)i('ll| will) (hurt|kill) myself if you"),
        ),

        // Romance scam
        Signal(
            "declares love implausibly fast", ThreatCategory.ROMANCE_SCAM, 15,
            Regex("(?i)i('m| am) (already |so )?in love with you|you('re| are) my soulmate|we('re| are) meant to be together"),
        ),
        Signal(
            "long-distance/overseas excuse", ThreatCategory.ROMANCE_SCAM, 15,
            Regex("(?i)stationed overseas|deployed (in|to|overseas)|working on an oil rig|on a military base"),
        ),
        Signal(
            "travel/customs money request", ThreatCategory.ROMANCE_SCAM, 35,
            Regex("(?i)customs fee|visa fee|stuck at the airport|need money to (come|visit|see) you|plane ticket money"),
        ),
        Signal(
            "secrecy about the relationship", ThreatCategory.ROMANCE_SCAM, 15,
            Regex("(?i)don'?t tell (anyone|your friends|your family) about us|keep (this|us) a secret"),
        ),
        Signal(
            "love bombing", ThreatCategory.ROMANCE_SCAM, 10,
            Regex("(?i)i('ve| have) never felt this way before|you('re| are) perfect for me"),
        ),

        // Grooming
        Signal(
            "secrecy from parents", ThreatCategory.GROOMING, 40,
            Regex("(?i)don'?t tell your (mom|dad|parents|mother|father)|this (is|has to be) (our|a) secret"),
        ),
        Signal(
            "asks if parents/guardians are around", ThreatCategory.GROOMING, 20,
            Regex("(?i)are your parents home|are you home alone|is anyone (else )?(there|home) with you"),
        ),
        Signal(
            "in-person meetup request", ThreatCategory.GROOMING, 30,
            Regex("(?i)can (i|we) meet you (in person|somewhere)|come (meet|see) me alone|meet up without (your|telling) (parents|anyone)"),
        ),
        Signal(
            "requests photos", ThreatCategory.GROOMING, 35,
            Regex("(?i)send (me )?(a |some )?(pic|pics|picture|pictures|photo|photos) of yourself|send (nudes|a nude)"),
        ),
        Signal(
            "gift or money for meeting", ThreatCategory.GROOMING, 25,
            Regex("(?i)i('ll| will) (buy|get|give) you (a gift|money|anything) if (you|we)"),
        ),
    )

    fun score(text: String): ScoreResult {
        if (text.isBlank()) {
            return ScoreResult(Severity.UNKNOWN, riskScore = 0, category = null, matchedSignals = emptyList())
        }

        val normalized = collapseRepeatedChars(text)
        val matched = signals.filter { it.pattern.containsMatchIn(normalized) }
        val total = matched.sumOf { it.weight }.coerceIn(0, 100)
        val severity = when {
            total >= 60 -> Severity.HIGH
            total >= 30 -> Severity.MEDIUM
            else -> Severity.SAFE
        }
        // dominant category = highest single contributor, for mixed matches
        val category = matched.maxByOrNull { it.weight }?.category

        return ScoreResult(severity, total, category, matched.map { it.label })
    }
}
