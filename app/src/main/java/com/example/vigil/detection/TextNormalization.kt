package com.example.vigil.detection
private val repeatedChar = Regex("(.)\\1{2,}")

fun collapseRepeatedChars(text: String): String = repeatedChar.replace(text) { it.groupValues[1] }
