package com.a303.helpmet.presentation.feature.preride

import androidx.lifecycle.ViewModel
import com.a303.helpmet.presentation.model.RideTimeWarning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RideTimeSetViewModel : ViewModel() {
    private val _rideTime = MutableStateFlow(60)
    val rideTime: StateFlow<Int> = _rideTime

    private val _warning = MutableStateFlow(RideTimeWarning.NONE)
    val warning: StateFlow<RideTimeWarning> = _warning

    // 최소 허용 시간
    var minTime = 10

    // 최대 허용 시간
    var maxTime = 300

    fun increaseTime(){
        _rideTime.value -= _rideTime.value % 10
        if(_rideTime.value+10 <= maxTime){
            _rideTime.value += 10
            _warning.value = RideTimeWarning.NONE
        }else{
            _warning.value = RideTimeWarning.TOO_LONG
        }

    }

    fun decreaseTime(){
        if(_rideTime.value % 10 != 0){
            _rideTime.value += 10 - (_rideTime.value % 10)
        }
        if(_rideTime.value-10 >= minTime){
            _rideTime.value -= 10
            _warning.value = RideTimeWarning.NONE
        }else{
            _warning.value = RideTimeWarning.TOO_SHORT
        }
    }

    fun setRideTime(time: Int){
        if(time >= minTime && time <= maxTime){
            _warning.value = RideTimeWarning.NONE
            _rideTime.value = time
        }else{
            if(time < minTime) _warning.value = RideTimeWarning.TOO_SHORT
            else if (time > maxTime) _warning.value = RideTimeWarning.TOO_LONG
        }
    }
}