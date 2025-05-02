package com.a303.helpmet.util.postPosition

fun appendTopicPostposition(word: String): String {
    val lastChar = word.last()
    val hasFinalConsonant = (lastChar.code - 0xAC00) % 28 != 0
    val postposition = if (hasFinalConsonant) "은" else "는"
    return "$word$postposition"
}