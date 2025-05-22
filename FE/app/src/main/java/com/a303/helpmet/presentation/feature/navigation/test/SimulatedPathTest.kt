package com.a303.helpmet.presentation.feature.navigation.test

import com.kakao.vectormap.LatLng
import kotlinx.coroutines.flow.MutableStateFlow

object SimulatedPathTest {
    val simulatedPath = listOf(
        LatLng.from(37.5016331, 127.0399224),
        LatLng.from(37.5012806, 127.0400408),
        LatLng.from(37.5009753, 127.0401896),
        LatLng.from(37.5009408, 127.0402094),
        LatLng.from(37.5008408, 127.0402094),
        LatLng.from(37.5007408, 127.0402094),
        LatLng.from(37.5006408, 127.0402094),
        LatLng.from(37.5005832, 127.0403949),
        LatLng.from(37.5005832, 127.0404949),
        LatLng.from(37.5005832, 127.0405949),
        LatLng.from(37.5005832, 127.0408949),
        LatLng.from(37.5005832, 127.0409949),
        LatLng.from(37.5005832, 127.0411949),
        LatLng.from(37.5008822, 127.0413418),
        LatLng.from(37.5010206, 127.0417805)
    )

    val currentIndex = MutableStateFlow(0)
}
