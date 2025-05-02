package com.a303.helpmet.util.postPosition

fun appendSubjectPostposition(word: String): String {
    if (word.isEmpty()) return word

    val lastChar = word.last()
    val isKorean = lastChar in '\uAC00'..'\uD7A3'
    val hasFinalConsonant = isKorean && (lastChar.code - 0xAC00) % 28 != 0
    val postposition = if (hasFinalConsonant) "이" else "가"
    return "$word$postposition"
}