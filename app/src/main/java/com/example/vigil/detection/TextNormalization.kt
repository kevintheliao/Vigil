package com.example.vigil.detection

/** Collapses runs of 3+ identical characters to one, so stretched-out spellings
 * ("stuuupiddd", "you'reeee") match the same vocab/signals as their normal form. */
private val repeatedChar = Regex("(.)\\1{2,}")

fun collapseRepeatedChars(text: String): String = repeatedChar.replace(text) { it.groupValues[1] }
