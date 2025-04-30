package com.a303.helpmet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckScreen
import com.a303.helpmet.presentation.feature.navigation.NavigationScreen
import com.a303.helpmet.presentation.feature.preride.PreRideScreen
import com.a303.helpmet.ui.theme.HelpmetTheme

// MainActivity.kt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HelpmetTheme {
                val navController = rememberNavController()
                // MainActivity.kt or AppNavGraph.kt 등
                NavHost(
                    navController = navController,
                    startDestination = "helmet_check"
                ) {
                    // 1) 헬멧 연결 확인 화면
                    composable("helmet_check") {
                        HelmetCheckScreen(
                            onSuccess = {
                                // 헬멧 연결 확인되면 2번 화면으로
                                navController.navigate("pre_ride")
                            }
                        )
                    }

                    // 2) 코스 안내 시작 전 화면
                    composable("pre_ride") {
                        PreRideScreen(
                            onStartRide = {
                                // 준비 완료(따릉이 빌림) 시 내비게이션 화면으로
                                navController.navigate("navigation")
                            }
                        )
                    }

                    // 3) 내비게이션 화면
                    composable("navigation") {
                        NavigationScreen(
                            onFinish = {
                                // 안내 종료 시 다시 2번(pre_ride) 화면으로
                                navController.popBackStack("pre_ride", inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }
}