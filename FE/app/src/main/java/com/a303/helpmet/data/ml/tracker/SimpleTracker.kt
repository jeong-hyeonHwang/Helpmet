package com.a303.helpmet.data.ml.tracker

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

data class TrackedObject(
    val id: Int,
    val rect: RectF,
    var timestamp: Long
)

class SimpleTracker {

    private var nextId = 0
    private val trackedObjects = mutableListOf<TrackedObject>()
    private val iouThreshold = 0.4f
    private val timeoutMs = 3000L // 오래된 트랙은 제거

    fun update(detections: List<RectF>): Map<Int, RectF> {
        val now = System.currentTimeMillis()
        val matched = mutableMapOf<Int, RectF>()
        val unmatched = detections.toMutableList()

        // 기존 트랙과 IoU 비교
        for (track in trackedObjects.toList()) {
            val bestMatch = unmatched.maxByOrNull { computeIoU(track.rect, it) ?: 0f }
            val iou = bestMatch?.let { computeIoU(track.rect, it) } ?: 0f

            if (iou >= iouThreshold) {
                matched[track.id] = bestMatch!!
                unmatched.remove(bestMatch)
                track.rect.set(bestMatch)
                track.timestamp = now
            }
        }

        // 매칭되지 않은 박스는 새 ID 할당
        for (newBox in unmatched) {
            val newId = nextId++
            trackedObjects.add(TrackedObject(newId, newBox, now))
            matched[newId] = newBox
        }

        // 오래된 트랙 제거
        trackedObjects.removeAll { now - it.timestamp > timeoutMs }

        return matched
    }

    private fun computeIoU(a: RectF, b: RectF): Float {
        val left = max(a.left, b.left)
        val top = max(a.top, b.top)
        val right = min(a.right, b.right)
        val bottom = min(a.bottom, b.bottom)

        val intersectionArea = max(0f, right - left) * max(0f, bottom - top)
        val unionArea = a.width() * a.height() + b.width() * b.height() - intersectionArea

        return if (unionArea <= 0f) 0f else intersectionArea / unionArea
    }
}