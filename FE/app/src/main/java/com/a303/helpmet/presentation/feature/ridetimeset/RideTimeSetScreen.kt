package com.a303.helpmet.presentation.feature.preride

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.presentation.model.RideTimeWarning
import com.a303.helpmet.ui.theme.HelpmetTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun RideTimeSetScreen(
    viewModel: RideTimeSetViewModel = koinViewModel(),
    onRideTimeSet : (Int) -> Unit
) {
    val rideTime by viewModel.rideTime.collectAsState()
    val warning by viewModel.warning.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(rideTime.toString()) }

    val warningText = when (warning) {
        RideTimeWarning.TOO_SHORT -> stringResource(R.string.error_min_time, viewModel.minTime)
        RideTimeWarning.TOO_LONG -> stringResource(R.string.error_max_time, viewModel.maxTime)
        else -> ""
    }

    Column(
        modifier = Modifier
            .background(HelpmetTheme.colors.white1)
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.set_ride_time),
                style = HelpmetTheme.typography.title,
                color = HelpmetTheme.colors.black1
            )
            Text(
                text = stringResource(R.string.prompt_set_time),
                style = HelpmetTheme.typography.bodySmall,
                color = HelpmetTheme.colors.gray2
            )
        }

        // 주행 시간 조정 영역
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {viewModel.decreaseTime()}) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_minus_circle),
                        contentDescription = "시간 감소",
                        tint = HelpmetTheme.colors.black1,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable {
                            isEditing = true
                            textFieldValue = rideTime.toString()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            TextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = HelpmetTheme.typography.display.copy(textAlign = TextAlign.Center),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = HelpmetTheme.colors.black1,
                                    focusedLeadingIconColor = HelpmetTheme.colors.black1,
                                    focusedTextColor = HelpmetTheme.colors.black1,
                                    unfocusedTextColor = HelpmetTheme.colors.black1,
                                    focusedTrailingIconColor = HelpmetTheme.colors.black1,
                                    focusedLabelColor = HelpmetTheme.colors.black1,
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val parsed = textFieldValue.toIntOrNull()
                                        if (parsed != null) {
                                            viewModel.setRideTime(parsed)
                                        }
                                        isEditing = false
                                    }
                                ),
                            )
                        }

                    } else {
                        Text(
                            text = stringResource(R.string.minutes_format, rideTime),
                            style = HelpmetTheme.typography.display,
                            color = HelpmetTheme.colors.black1,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                IconButton(onClick = { viewModel.increaseTime() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_plus_circle),
                        contentDescription = "시간 증가",
                        tint = HelpmetTheme.colors.black1,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Text(
                text = warningText,
                style = HelpmetTheme.typography.bodySmall,
                color = HelpmetTheme.colors.red1
            )
        }

        Button (
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HelpmetTheme.colors.black1
            ),
            onClick = { onRideTimeSet (rideTime) }
        ) {
            Text(
                text = stringResource(R.string.start_course_recommend),
                style = HelpmetTheme.typography.subtitle,
                color = HelpmetTheme.colors.primaryNeon
            )
        }
    }
}

@Preview
@Composable
fun RideTimeSettingPreview() {
    RideTimeSetScreen(
        viewModel = RideTimeSetViewModel(),
        onRideTimeSet = {}
    )
}