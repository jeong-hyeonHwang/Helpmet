package com.a303.helpmet.util.extension

fun String.trimLocationName(): String {
    val tokens = this.split(" ")
    return if (tokens.size <= 4) this else tokens.takeLast(4).joinToString(" ")
}
