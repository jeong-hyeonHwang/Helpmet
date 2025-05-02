package com.a303.helpmet.util.postPosition

fun appendObjectPostposition(word: String): String {
    val lastChar = word.last()
    val hasFinalConsonant = (lastChar.code - 0xAC00) % 28 != 0
    val postposition = if (hasFinalConsonant) "을" else "를"
    return "$word$postposition"
}