package com.a303.helpmet.data.ml.analysis

import android.graphics.RectF


data class TrackedBox(val rect: RectF, val timestamp: Long)

class ApproachAnalyzer {

    private val history = mutableMapOf<Int, MutableList<TrackedBox>>()
    private val maxHistorySize = 5
    private val approachThreshold = 0.2f // 20% 이상 확대되면 접근으로 간주
    private val staleTimeout = 3000L // 3초 이상 지난 트랙은 삭제

    fun addDetection(trackId: Int, rect: RectF): Boolean {
        val now = System.currentTimeMillis()

        // 오래된 트랙 제거
        history.entries.removeIf { (_, list) ->
            list.lastOrNull()?.timestamp?.let { now - it > staleTimeout } == true
        }

        val list = history.getOrPut(trackId) { mutableListOf() }
        list.add(TrackedBox(rect, now))
        if (list.size > maxHistorySize) list.removeAt(0)

        return isApproaching(list)
    }

    private fun isApproaching(history: List<TrackedBox>): Boolean {
        if (history.size < maxHistorySize) return false
        val start = area(history.first().rect)
        val end = area(history.last().rect)
        if (start <= 0f) return false
        val change = (end - start) / start
        return change > approachThreshold
    }

    private fun area(rect: RectF): Float = rect.width() * rect.height()
}
