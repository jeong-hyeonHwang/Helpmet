package com.a303.helpmet.data.ml.analysis

import android.graphics.RectF

data class TrackedBox(val trackId: Int, val rect: RectF, val frameTime: Long)

class ApproachAnalyzer {
    private val history = mutableMapOf<Int, MutableList<TrackedBox>>()

    fun addDetection(trackId: Int, rect: RectF): Boolean {
        val now = System.currentTimeMillis()
        val entry = TrackedBox(trackId, rect, now)
        val list = history.getOrPut(trackId) { mutableListOf() }
        list.add(entry)
        if (list.size > 5) list.removeAt(0)

        return isApproaching(list)
    }

    private fun isApproaching(history: List<TrackedBox>): Boolean {
        if (history.size < 5) return false
        val startArea = area(history.first().rect)
        val endArea = area(history.last().rect)
        val change = (endArea - startArea) / startArea
        return change > 0.2f // 20% 이상 확대 → 접근 판단
    }

    private fun area(r: RectF): Float = r.width() * r.height()
}
