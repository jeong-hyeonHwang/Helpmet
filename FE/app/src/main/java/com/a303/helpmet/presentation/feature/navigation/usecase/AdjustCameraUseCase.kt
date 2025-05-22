package com.a303.helpmet.presentation.feature.navigation.usecase

import com.a303.helpmet.presentation.model.LatLngUi
import com.a303.helpmet.util.GeoUtils
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.camera.CameraUpdateFactory

class AdjustCameraUseCase {
    operator fun invoke(
        kakaoMap: KakaoMap,
        currentPosition: LatLng,
        from: LatLng,
        to: LatLngUi,
        zoom: Int
    ) {
        val bearing = GeoUtils.calculateBearing(from, to)
        val corrected = (360.0 - bearing) % 360.0
        val cameraPosition = CameraPosition.from(
            CameraPosition.Builder()
                .setPosition(currentPosition)
                .setZoomLevel(zoom)
                .setRotationAngle(Math.toRadians(corrected))
                .setTiltAngle(Math.toRadians(30.0))
        )
        kakaoMap.moveCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            CameraAnimation.from(500)
        )
    }
}
