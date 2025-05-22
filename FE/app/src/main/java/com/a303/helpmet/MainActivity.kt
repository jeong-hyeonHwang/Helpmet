package com.a303.helpmet
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckScreen
import com.a303.helpmet.presentation.feature.navigation.ui.NavigationScreen
import com.a303.helpmet.presentation.feature.preride.PreRideScreen
import com.a303.helpmet.presentation.feature.preride.RideTimeSetScreen
import com.a303.helpmet.presentation.feature.voiceguide.VoiceGuideScreen
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.framework.notification.NotificationChannelManager
import com.a303.helpmet.framework.usage.service.AppUsageService
import com.a303.helpmet.framework.usage.util.UsageAccessManager
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.kakao.sdk.common.util.Utility.getKeyHash
import org.koin.androidx.viewmodel.ext.android.viewModel

// MainActivity.kt

class MainActivity : ComponentActivity() {

    private var hasCheckedUsageAccess = false  // 설정 복귀 감지용 플래그
    private lateinit var receiver: BroadcastReceiver
    val voiceInteractViewModel: VoiceInteractViewModel by viewModel()
    val routeViewModel: RouteViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val keyHash = getKeyHash(this)
        Log.d("HASH", keyHash)

        NotificationChannelManager.setupNotificationChannels(this)

        if (!UsageAccessManager.hasUsageAccess(this)) {
            UsageAccessManager.showPermissionDialog(this) {
                UsageAccessManager.requestUsageAccessPermission(this)
            }
        } else {
            hasCheckedUsageAccess = true
            startAppUsageService()
        }

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
                            onHelmetChecked = {
                                // 주행 코스 추천 화면 이동 전 주행 시간 설정 화면으로
                                navController.navigate("ride_time_set")
                            },
                            onStartVoiceGuide = {
                                // 음성 가이드 화면으로 이동
                                navController.navigate("voice_guide")
                            }
                        )
                    }

                    // 2-1) 코스 추천 전 주행 시간 설정 화면
                    composable("ride_time_set") {
                        RideTimeSetScreen(
                            onRideTimeSet = { rideTime ->
                                // 주행 시간 설정되면 3번 화면으로
                                navController.navigate("pre_ride?rideTime=${rideTime}")
                            }
                        )
                    }

                    // 2-2) 음성 정보 가이드 화면
                    composable("voice_guide") {
                        VoiceGuideScreen()
                    }

                    // 3) 코스 안내 시작 전 화면
                    composable(
                        "pre_ride?rideTime={rideTime}",
                        arguments = listOf(navArgument("rideTime") { type = NavType.IntType; defaultValue = 30 })
                    ) { backStackEntry ->
                        val rideTime = backStackEntry.arguments?.getInt("rideTime") ?: 30

                        PreRideScreen(
                            rideTime = rideTime,
                            onStartRide = {
                                navController.navigate("navigation")
                            }
                        )
                    }

                    // 4) 내비게이션 화면
                    composable("navigation") {
                        NavigationScreen(
                            onFinish = {
                                // 안내 종료 시 다시 1번(helmet_check) 화면으로
                                navController.popBackStack("helmet_check", inclusive = false)
                            },
                            navController = navController,
                            routeViewModel = routeViewModel
                        )
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        if (!hasCheckedUsageAccess && UsageAccessManager.hasUsageAccess(this)) {
            Toast.makeText(this, this.getString(R.string.permission_complete), Toast.LENGTH_SHORT).show()
            hasCheckedUsageAccess = true

            startAppUsageService()
        }
    }

    private fun startAppUsageService() {
        val intent = Intent(this, AppUsageService::class.java)
        startService(intent)
    }
}