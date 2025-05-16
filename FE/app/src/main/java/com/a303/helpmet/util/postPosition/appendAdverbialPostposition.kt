package com.a303.helpmet.util.postPosition

fun appendAdverbialPostposition(word: String): String {
    if (word.isEmpty()) return word

    val lastChar = word.last()
    val isKorean = lastChar in '\uAC00'..'\uD7A3'
    val hasFinalConsonant = isKorean && (lastChar.code - 0xAC00) % 28 != 0

    // 예외 처리: 받침 'ㄹ'인 경우에는 '로' 사용
    val isRieulEnding = isKorean && ((lastChar.code - 0xAC00) % 28 == 8)

    val postposition = when {
        isRieulEnding -> "로"
        hasFinalConsonant -> "으로"
        else -> "로"
    }

    return "$word$postposition"
}
