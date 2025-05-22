package com.a303.helpmet.util.postPosition

fun appendTopicPostposition(word: String): String {
    if (word.isEmpty()) return word

    val lastChar = word.last()
    val isKorean = lastChar in '\uAC00'..'\uD7A3'
    val hasFinalConsonant = isKorean && (lastChar.code - 0xAC00) % 28 != 0
    val postposition = if (hasFinalConsonant) "은" else "는"
    return "$word$postposition"
}