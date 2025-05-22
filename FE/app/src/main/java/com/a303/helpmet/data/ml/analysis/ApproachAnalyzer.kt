package com.a303.helpmet.data.ml.analysis

import android.graphics.RectF
import kotlin.math.max

data class TrackedBox(
    val rect: RectF,
    val timestamp: Long
)

class ApproachAnalyzer {
    private val history = mutableMapOf<Int, MutableList<TrackedBox>>()
    private val maxHistorySize = 3
    private val staleTimeout = 3000L

    private val stage1Threshold = 0.15f
    private val stage2Threshold = 0.5f
    private val yStage1Threshold = 0.7f
    private val yStage2Threshold = 0.85f

    fun getApproachLevel(trackId: Int, rect: RectF): Int {
        val now = System.currentTimeMillis()
        history.entries.removeIf { (_, list) ->
            list.lastOrNull()?.timestamp?.let { now - it > staleTimeout } == true
        }

        val list = history.getOrPut(trackId) { mutableListOf() }
        list.add(TrackedBox(rect, now))
        if (list.size > maxHistorySize) list.removeAt(0)

        return evaluateStage(list)
    }

    private fun evaluateStage(history: List<TrackedBox>): Int {
        if (history.size < maxHistorySize) return 0

        val startArea = area(history.first().rect)
        val endArea = area(history.last().rect)
        if (startArea <= 0f) return 0

        val sizeChange = (endArea - startArea) / startArea
        val rect = history.last().rect
        val y2Ratio = rect.bottom / 320f

        val sizeLevel = when {
            sizeChange > stage2Threshold -> 2
            sizeChange > stage1Threshold -> 1
            else -> 0
        }

        val yLevel = when {
            y2Ratio > yStage2Threshold -> 2
            y2Ratio > yStage1Threshold -> 1
            else -> 0
        }

        return max(sizeLevel, yLevel)
    }

    private fun area(rect: RectF): Float = rect.width() * rect.height()
}