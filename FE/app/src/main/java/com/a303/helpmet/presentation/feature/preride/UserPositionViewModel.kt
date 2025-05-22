package com.a303.helpmet.presentation.feature.preride

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class UserPositionViewModel : ViewModel() {

    private val _position = MutableStateFlow(LatLng.from(0.0, 0.0))
    val position: StateFlow<LatLng> = _position.asStateFlow()

    private val _heading = MutableStateFlow(0f) // 0 ~ 360
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _isUserInteracting = MutableStateFlow(false)
    val isUserInteracting: StateFlow<Boolean> = _isUserInteracting.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var sensorListener: SensorEventListener? = null
    private var lastSmoothedHeading = 0f
    private var isFirstSensorSample = true

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startTracking(context: Context, onUpdate: (LatLng, Float) -> Unit) {
        // ① 위치 추적 설정
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val req = LocationRequest.create().apply {
            interval = 1000L
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
        fused.requestLocationUpdates(req, locationCallback!!, Looper.getMainLooper())

        // ② 센서 추적 설정
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

                val rotationMatrix = FloatArray(9)
                val remappedMatrix = FloatArray(9)
                val orientation = FloatArray(3)

                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                // 디스플레이 방향에 맞춰 좌표계 재정렬
                when (wm.defaultDisplay.rotation) {
                    Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedMatrix)
                    Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remappedMatrix)
                    Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Z, remappedMatrix)
                    Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X, remappedMatrix)
                    else -> rotationMatrix.copyInto(remappedMatrix)
                }

                SensorManager.getOrientation(remappedMatrix, orientation)

                val azimuthRad = orientation[0]
                val pitch = Math.toDegrees(orientation[1].toDouble())
                val roll = Math.toDegrees(orientation[2].toDouble())

                // ③ 기울기가 너무 큰 경우 방위각 무시
                if (abs(pitch) > 85.0 || abs(roll) > 85.0) return

                val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()) + 360.0) % 360.0
                val corrected = azimuthDeg.toFloat()

                val smoothed = if (isFirstSensorSample) {
                    isFirstSensorSample = false
                    corrected
                } else {
                    interpolateAngle(lastSmoothedHeading, corrected)
                }

                lastSmoothedHeading = smoothed
                _heading.value = smoothed
                onUpdate(_position.value, smoothed)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sm.registerListener(
            sensorListener,
            sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun setUserInteracting(interacting: Boolean) {
        _isUserInteracting.value = interacting
    }

    // TEST: 테스트용 위치 설정을 위한 메소드
    fun setMockPosition(p: LatLng) {
        _position.value = p
    }

    private fun interpolateAngle(old: Float, new: Float): Float {
        val delta = ((new - old + 540f) % 360f) - 180f
        return (old + 0.15f * delta + 360f) % 360f
    }
}
