package com.example.vigil.detection

import java.text.Normalizer

/** Undoes common scam obfuscation before classification/scoring: invisible characters, homoglyphs, letter-spaced keywords. */
object TextNormalizer {
    private val invisibleChars = Regex("[\\u200B-\\u200D\\uFEFF\\u2060]")

    // matches "f r e e" / "g.i.f.t" style spacing: 4+ chars joined by the SAME separator throughout,
    // so it won't swallow a preceding word (mixed separators) or short real abbreviations like "U.S."
    private val spacedOutWord = Regex("(?i)\\b(\\w)([ .\\-_])(?:\\w\\2){2,}\\w\\b")

    fun normalize(text: String): String {
        val nfkc = Normalizer.normalize(text, Normalizer.Form.NFKC)
        val noInvisible = invisibleChars.replace(nfkc, "")
        return spacedOutWord.replace(noInvisible) { match -> match.value.filter { it.isLetterOrDigit() } }
    }
}
