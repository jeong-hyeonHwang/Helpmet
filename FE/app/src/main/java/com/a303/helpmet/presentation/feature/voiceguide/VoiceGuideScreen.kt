package com.a303.helpmet.presentation.feature.voiceguide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme


@Composable
fun VoiceGuideScreen() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp),

        ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.voice_guide),
                style = HelpmetTheme.typography.title,
                color = HelpmetTheme.colors.black1
            )
            Text(
                text = stringResource(R.string.prompt_voice_guide),
                style = HelpmetTheme.typography.bodySmall,
                color = HelpmetTheme.colors.gray2
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.voice_guide_turn_signal),
                    style = HelpmetTheme.typography.headline,
                    color = HelpmetTheme.colors.primaryNeon,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = HelpmetTheme.colors.black1, shape = RoundedCornerShape(
                                topStart = 25.dp,
                                topEnd = 25.dp,
                                bottomEnd = 25.dp,
                                bottomStart = 0.dp
                            )
                        )
                        .padding(15.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp, shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            color = HelpmetTheme.colors.white1,
                        )

                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_helmet),
                        contentDescription = "헬멧",
                        tint = HelpmetTheme.colors.black1,
                        modifier = Modifier.size(80.dp).padding(5.dp)
                    )

                    Text(
                        text = stringResource(R.string.prompt_voice_guide_turn_signal_helmet),
                        style = HelpmetTheme.typography.bodyLarge,
                        color = HelpmetTheme.colors.black1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()

                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp, shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            color = HelpmetTheme.colors.white1,
                        )

                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_left_direction_arrow),
                        contentDescription = "방향 지시등",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(80.dp)
                    )

                    Text(
                        text = stringResource(R.string.prompt_voice_guide_turn_signal_app),
                        style = HelpmetTheme.typography.bodyLarge,
                        color = HelpmetTheme.colors.black1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()

                    )
                }
            }


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.voice_guide_toilet),
                    style = HelpmetTheme.typography.headline,
                    color = HelpmetTheme.colors.primaryNeon,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = HelpmetTheme.colors.black1, shape = RoundedCornerShape(
                                topStart = 25.dp,
                                topEnd = 25.dp,
                                bottomEnd = 25.dp,
                                bottomStart = 0.dp
                            )
                        )
                        .padding(15.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp, shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            color = HelpmetTheme.colors.white1,
                        )

                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_toilet),
                        contentDescription = "화장실",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(80.dp).padding(10.dp)
                    )

                    Text(
                        text = stringResource(R.string.prompt_voice_guide_toilet),
                        style = HelpmetTheme.typography.bodyLarge,
                        color = HelpmetTheme.colors.black1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()

                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.voice_guide_rental),
                    style = HelpmetTheme.typography.headline,
                    color = HelpmetTheme.colors.primaryNeon,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = HelpmetTheme.colors.black1, shape = RoundedCornerShape(
                                topStart = 25.dp,
                                topEnd = 25.dp,
                                bottomEnd = 25.dp,
                                bottomStart = 0.dp
                            )
                        )
                        .padding(15.dp)
                )



                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp, shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            color = HelpmetTheme.colors.white1,
                        )

                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_bikeseoul),
                        contentDescription = "따릉이",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(80.dp)
                    )

                    Text(
                        text = stringResource(R.string.prompt_voice_guide_rental),
                        style = HelpmetTheme.typography.bodyLarge,
                        color = HelpmetTheme.colors.black1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()

                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.voice_guide_end),
                    style = HelpmetTheme.typography.headline,
                    color = HelpmetTheme.colors.primaryNeon,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = HelpmetTheme.colors.black1, shape = RoundedCornerShape(
                                topStart = 25.dp,
                                topEnd = 25.dp,
                                bottomEnd = 25.dp,
                                bottomStart = 0.dp
                            )
                        )
                        .padding(15.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp, shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            color = HelpmetTheme.colors.white1,
                        )

                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_end),
                        contentDescription = "안내 종료",
                        tint = HelpmetTheme.colors.black1,
                        modifier = Modifier.size(80.dp).padding(12.dp)
                    )

                    Text(
                        text = stringResource(R.string.prompt_voice_guide_end),
                        style = HelpmetTheme.typography.bodyLarge,
                        color = HelpmetTheme.colors.black1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()

                    )
                }

            }
        }
    }
}
