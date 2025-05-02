package com.a303.helpmet.util.postPosition

fun appendSubjectPostposition(word: String): String {
    val lastChar = word.last()
    val hasFinalConsonant = (lastChar.code - 0xAC00) % 28 != 0
    val postposition = if (hasFinalConsonant) "이" else "가"
    return "$word$postposition"
}