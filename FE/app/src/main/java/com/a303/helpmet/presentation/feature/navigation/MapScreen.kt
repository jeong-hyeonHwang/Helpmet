package com.a303.helpmet.presentation.feature.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.camera.CameraUpdateFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun MapScreen(
    defaultPosition: LatLng = LatLng.from(37.394660, 127.111182),
    defaultZoom: Int = 15
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 0) 권한 상태
    var hasLocationPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (!granted) {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 진입 시 권한 요청
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 1) KakaoMap 인스턴스 보관
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    // 2) MapView 초기화
    val mapView = remember {
        MapView(context).also { mv ->
            // Lifecycle 연결
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) = mv.resume()
                override fun onPause(owner: LifecycleOwner)  = mv.pause()
                override fun onDestroy(owner: LifecycleOwner) = mv.finish()
            })

            // start() 호출
            mv.start(
                object : MapLifeCycleCallback() {
                    override fun onMapResumed() { }

                    override fun onMapPaused() { }

                    override fun onMapDestroy() { }

                    override fun onMapError(error: Exception?) { }
                },
                object : KakaoMapReadyCallback() {
                    override fun onMapReady(map: KakaoMap) {
                        kakaoMap = map
                        // 초기 카메라 이동
                        val camPos = CameraPosition.from(
                            CameraPosition.Builder()
                                .setPosition(defaultPosition)
                                .setZoomLevel(defaultZoom)
                                .setTiltAngle(0.0)
                                .setRotationAngle(0.0)
                        )
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos))
                    }

                    override fun getPosition(): LatLng = defaultPosition
                    override fun getZoomLevel(): Int = defaultZoom
                }
            )
        }
    } // ← remember 블록 닫기

    // 3) 권한이 있고 kakaoMap 이 준비되면 사용자 위치로 중앙 이동
    LaunchedEffect(kakaoMap, hasLocationPermission) {
        if (kakaoMap != null && hasLocationPermission) {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val loc = suspendCancellableCoroutine<Location?> { cont ->
                    client.lastLocation
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resume(null) }
                }
                loc?.let {
                    val userPos = LatLng.from(it.latitude, it.longitude)
                    val camPos = CameraPosition.from(
                        CameraPosition.Builder()
                            .setPosition(userPos)
                            .setZoomLevel(defaultZoom)
                            .setTiltAngle(0.0)
                            .setRotationAngle(0.0)
                    )
                    kakaoMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(camPos))
                }
            } catch (e: SecurityException) {
                // 권한이 사라졌을 경우
                Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4) MapView를 Compose UI에 삽입
    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}
