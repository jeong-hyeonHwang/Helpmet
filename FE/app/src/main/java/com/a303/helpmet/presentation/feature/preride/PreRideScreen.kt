package com.a303.helpmet.presentation.feature.preride

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.data.dto.response.CourseResponse
import com.a303.helpmet.domain.mapper.toCourseInfo
import com.a303.helpmet.presentation.feature.preride.component.*

@Composable
fun PreRideScreen(
    onStartRide: (Int) -> Unit
) {
    val dummyCourses = listOf(
        CourseResponse(
            courseNumber = 1,
            duration = 15,
            distanceKm = 8.5f,
            startStation = "연제구 꽃잎길 32",
            endStation = "성동구 와우로길 32길",
            navId = 1
        ),
        CourseResponse(
            courseNumber = 2,
            duration = 30,
            distanceKm = 10.5f,
            startStation = "연제구 꽃잎길 32",
            endStation = "성동구 와우로길 32길",
            navId = 2
        ),
        CourseResponse(
            courseNumber = 3,
            duration = 10,
            distanceKm = 6f,
            startStation = "연제구 꽃잎길 32",
            endStation = "성동구 와우로길 32길",
            navId = 3
        )
    ).map { it.toCourseInfo(LocalContext.current) }

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }

    // 이어폰 연결 확인 함수
    fun isHeadsetConnected(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices: Array<AudioDeviceInfo> =
            audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)

        return devices.any { device ->
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> true

                else -> false
            }
        }
    }

    val onStartRideClicked: (Int) -> Unit = { courseId ->
        val isConnected = isHeadsetConnected()
        if (isConnected) {
            selectedCourseId = courseId
            showDialog = true
        } else {
            onStartRide(courseId)
        }
    }
    if (showDialog && selectedCourseId != null) {
        NoiseCancelingWarningDialog(
            onDismiss = {showDialog = false},
            onConfirm = {
                selectedCourseId?.let {
                    showDialog = false
                    onStartRide(it)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            PreRideMapView()
            CourseInfoBubbleView(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )

            CourseCardPager(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                courses = dummyCourses,
                onStartRide = onStartRideClicked
            )
        }
    }
}

@Preview
@Composable
fun PreRidePreview() {
    PreRideScreen(
        onStartRide = {}
    )
}