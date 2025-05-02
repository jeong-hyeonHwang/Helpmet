package com.a303.helpmet.presentation.feature.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.*
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.shape.DotPoints
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.Polygon
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyle
import com.kakao.vectormap.shape.ShapeLayer

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("RememberReturnType")
@Composable
fun MapScreen(
    defaultPosition: LatLng = LatLng.from(37.394660, 127.111182),
    defaultZoom: Int = 17
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 0) 권한 상태 및 요청
    var hasLocationPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (!granted) Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(Unit) { if (!hasLocationPermission) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    // 상태 변수: 맵, 도형, 위치, 헤딩
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var layer by remember { mutableStateOf<ShapeLayer?>(null) }
    var circlePoly by remember { mutableStateOf<Polygon?>(null) }
    var triPoly by remember { mutableStateOf<Polygon?>(null) }
    var currentPosition by remember { mutableStateOf(defaultPosition) }
    var currHead by remember { mutableStateOf(0f) }

    // 1) location updates
    val locationRequest = remember {
        LocationRequest.create().apply {
            interval = 2000L; fastestInterval = 1000L
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val userPos = LatLng.from(loc.latitude, loc.longitude)

                currentPosition = userPos

                // 원 업데이트
                circlePoly!!.setPosition(currentPosition)
                // 삼각형 업데이트
                triPoly?.changeMapPoints(listOf(MapPoints.fromLatLng(makeTriPoints(userPos, currHead))))

                // 카메라 중앙 이동
                kakaoMap?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(userPos, defaultZoom),
                    CameraAnimation.from(300)
                )
            }
        }
    }

    // 2) MapView 초기화 및 도형 생성
    val mapView = remember {
        MapView(context).also { mv ->
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) = mv.resume()
                override fun onPause(owner: LifecycleOwner) = mv.pause()
                override fun onDestroy(owner: LifecycleOwner) = mv.finish()
            })

            mv.start(
                object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {}
                    override fun onMapError(e: Exception) {}
                },
                object : com.kakao.vectormap.KakaoMapReadyCallback() {
                    override fun onMapReady(map: KakaoMap) {
                        kakaoMap = map
                        layer = map.shapeManager?.getLayer()

                        // 초기 원
                        circlePoly = layer?.addPolygon(
                            PolygonOptions.from(
                                DotPoints.fromCircle(defaultPosition, RADIUS),
                                PolygonStyle.from(0x5533B5E5)
                            )
                        )
                        // 초기 삼각형
                        triPoly = layer?.addPolygon(
                            PolygonOptions.from(
                                MapPoints.fromLatLng(makeTriPoints(defaultPosition, 0f)),
                                PolygonStyle.from(0xAAFF0000.toInt())
                            )
                        )
                        // 카메라 초기 위치
                        map.moveCamera(CameraUpdateFactory.newCenterPosition(defaultPosition, defaultZoom))
                    }
                    override fun getPosition(): LatLng = defaultPosition
                    override fun getZoomLevel(): Int = defaultZoom
                }
            )
        }
    }

    // 3) 위치 업데이트 시작/해제
    DisposableEffect(hasLocationPermission, kakaoMap) {
        if (hasLocationPermission && kakaoMap != null) {
            LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        onDispose {
            LocationServices.getFusedLocationProviderClient(context)
                .removeLocationUpdates(locationCallback)
        }
    }

    // 4) 센서 이벤트 처리
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val rotSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    DisposableEffect(kakaoMap) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val m = FloatArray(9); val o = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(m, e.values)
                    SensorManager.getOrientation(m, o)
                    currHead = ((Math.toDegrees(o[0].toDouble()) + 360) % 360).toFloat()
                    triPoly?.changeMapPoints(
                        mutableListOf(
                            MapPoints.fromLatLng(
                                makeTriPoints(
                                    currentPosition,
                                    currHead
                                ))))
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, rotSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // 5) Compose에 MapView 표시
    AndroidView(
        factory = { mapView },
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { motionEvent ->
                mapView.dispatchTouchEvent(motionEvent)
                true
            }
    )
}

private const val RADIUS = 30f
private const val TRI_F = 65f
private const val TRI_S = 45f

private fun makeTriPoints(center: LatLng, heading: Float): List<LatLng> {
    val forward = offset(center, TRI_F, heading)
    val left = offset(center, TRI_S, heading - 15)
    val right = offset(center, TRI_S, heading + 15)
    return listOf(forward, left, right, forward)
}

private fun offset(o: LatLng, d: Float, hdg: Float): LatLng {
    val rad = Math.toRadians(hdg.toDouble())
    val dy = d * Math.cos(rad)
    val dx = d * Math.sin(rad)
    val dLat = dy / 111000.0
    val dLng = dx / (111000.0 * Math.cos(Math.toRadians(o.latitude)))
    return LatLng.from(o.latitude + dLat, o.longitude + dLng)
}
