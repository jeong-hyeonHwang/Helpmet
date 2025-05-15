package com.a303.helpmet.data.ml.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

class YoloV5TFLiteDetector(context: Context) {

    private val imageSize = 320 // YOLOv5n 입력 크기
    private val interpreter: Interpreter = Interpreter(loadModelFile(context, "yolov5n.tflite"))

    data class DetectionResult(
        val classId: Int,
        val score: Float,
        val rect: RectF
    )

    fun detect(bitmap: Bitmap): List<DetectionResult> {

        var startTime = System.currentTimeMillis()
//        Log.d("WebSocket", "객체 탐지 시작")

        val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
        val input = bitmapToByteBuffer(resized)
        val output = Array(1) { Array(6300) { FloatArray(85) } }

        interpreter.run(input, output)

        var endTime = System.currentTimeMillis()

//        Log.d("WebSocket", "객체 탐지에 소요된 시간: "+(endTime-startTime))
        return postProcess(output[0])
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val input = ByteBuffer.allocateDirect(1 * imageSize * imageSize * 3 * 4)
        input.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSize * imageSize)
        bitmap.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)
        for (pixel in pixels) {
            input.putFloat(((pixel shr 16 and 0xFF) / 255.0f)) // R
            input.putFloat(((pixel shr 8 and 0xFF) / 255.0f))  // G
            input.putFloat(((pixel and 0xFF) / 255.0f))        // B
        }

        input.rewind()
        return input
    }

    private fun postProcess(predictions: Array<FloatArray>): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        val allowedClasses = setOf(0, 1, 2) // 사람, 자전거, 자동차

        for (row in predictions) {
            val objectness = row[4]
            if (objectness < 0.4f) continue

            val classScores = row.copyOfRange(5, 85)
            val maxIdx = classScores.indices.maxByOrNull { classScores[it] } ?: continue
            val classScore = classScores[maxIdx]
            if (classScore < 0.25f) continue
            if (maxIdx !in allowedClasses) continue // 허락된 클래스만 체크하기

            val cx = row[0] * imageSize
            val cy = row[1] * imageSize
            val w = row[2] * imageSize
            val h = row[3] * imageSize

            val left = max(0f, cx - w / 2)
            val top = max(0f, cy - h / 2)
            val right = min(imageSize.toFloat(), cx + w / 2)
            val bottom = min(imageSize.toFloat(), cy + h / 2)

            results.add(
                DetectionResult(
                    classId = maxIdx,
                    score = objectness * classScore,
                    rect = RectF(left, top, right, bottom)
                )
            )
        }

        return results
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
