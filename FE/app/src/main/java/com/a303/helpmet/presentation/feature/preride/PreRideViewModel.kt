package com.a303.helpmet.presentation.feature.preride

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreRideViewModel : ViewModel() {
    private val _courseId = MutableStateFlow("default_course")
    val courseId: StateFlow<String> = _courseId

    fun selectCourse(id: String) {
        _courseId.value = id
    }
}