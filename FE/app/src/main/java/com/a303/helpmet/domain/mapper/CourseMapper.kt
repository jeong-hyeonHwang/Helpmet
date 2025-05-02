package com.a303.helpmet.domain.mapper

import android.content.Context
import com.a303.helpmet.R
import com.a303.helpmet.data.dto.response.CourseResponse
import com.a303.helpmet.presentation.model.CourseInfo

fun CourseResponse.toCourseInfo(context: Context): CourseInfo {
    return CourseInfo(
        courseName = context.getString(R.string.course_number, courseNumber),
        duration = context.getString(R.string.course_duration, duration),
        distanceKm = context.getString(R.string.course_distance, distanceKm),
        startStation = context.getString(R.string.start_station, startStation),
        endStation = context.getString(R.string.end_station, endStation),
        navId = navId
    )
}
