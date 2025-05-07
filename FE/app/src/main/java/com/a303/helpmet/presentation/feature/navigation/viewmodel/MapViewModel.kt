package com.a303.helpmet.presentation.feature.navigation.viewmodel

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {
    private val _position = MutableStateFlow<LatLng>(LatLng.from(37.394660,127.111182))
    val position: StateFlow<LatLng> = _position.asStateFlow()

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _isUserInteracting = MutableStateFlow(false)
    val isUserInteracting: StateFlow<Boolean> = _isUserInteracting.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var sensorListener: SensorEventListener? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startTracking(
        context: Context,
        onUpdate: (LatLng, Float) -> Unit
    ) {
        // 위치 업데이트 등록
        val client = LocationServices.getFusedLocationProviderClient(context)
        val req = LocationRequest.create().apply {
            interval = 2000L
            fastestInterval = 1000L
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    val pos = LatLng.from(it.latitude, it.longitude)
                    _position.value = pos
                    onUpdate(pos, _heading.value)
                }
            }
        }
        client.requestLocationUpdates(req, locationCallback!!, Looper.getMainLooper())

        // 센서 등록
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val m = FloatArray(9)
                    val o = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(m, e.values)
                    SensorManager.getOrientation(m, o)
                    val hd = ((Math.toDegrees(o[0].toDouble()) + 360) % 360).toFloat()
                    _heading.value = hd
                    onUpdate(_position.value, hd)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    /**
     * 사용자의 지도 상호작용 상태 설정
     */
    fun setUserInteracting(isInteracting: Boolean) {
        _isUserInteracting.value = isInteracting
    }

    override fun onCleared() {
        // 위치·센서 해제 로직
        super.onCleared()
    }
}
