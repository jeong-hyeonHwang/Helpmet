package com.a303.helpmet.presentation.feature.navigation.component

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.presentation.feature.navigation.viewmodel.DetectionViewModel
import com.a303.helpmet.R
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.handler.getGatewayIp
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient

@Composable
fun StreamingView(
    navigationViewModel: NavigationViewModel = org.koin.androidx.compose.koinViewModel(),
    detectionViewModel: DetectionViewModel,
    webPageUrl: String
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val streamingViewHeight = screenWidth * 3 / 4

    val isActiveStreamingView by navigationViewModel.isActiveStreamingView.collectAsState()

    val isValidPi by navigationViewModel.isValidPi.collectAsState()
    val isAccessible by navigationViewModel.isAccessible.collectAsState()

    // ✅ 최초 실행 시 WebSocket 연결 준비


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HelpmetTheme.colors.gray1)
            .requiredHeightIn(max = streamingViewHeight)
            .height(if (isActiveStreamingView) streamingViewHeight else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {

        when (isValidPi) {
            null -> {
                Text(
                    text = "라즈베리파이를 찾는 중입니다...",
                    style = HelpmetTheme.typography.title,
                    color = HelpmetTheme.colors.primaryLight
                )
            }
            true -> {
                if (isAccessible) WebRTCPage(url = webPageUrl)
                else {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_helmet),
                        contentDescription = "사용중",
                        tint = HelpmetTheme.colors.primaryLight,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "누군가가 헬프멧을 사용하고 있습니다.",
                        style = HelpmetTheme.typography.title,
                        color = HelpmetTheme.colors.primaryLight,
                    )
                }

            }
            false -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_util_helmet),
                    contentDescription = "연결 필요",
                    tint = HelpmetTheme.colors.primaryLight,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "헬프멧과 연결되지 않았습니다.",
                    style = HelpmetTheme.typography.title,
                    color = HelpmetTheme.colors.primaryLight,
                )
            }
        }
    }
}

@Composable
fun WebRTCPage(url: String) {
    val context = LocalContext.current
    val gatewayIp = getGatewayIp(context)
    val wsUrl = "ws://$gatewayIp:${BuildConfig.SOCKET_PORT}/ws"

    LaunchedEffect(url) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiNetwork = getWifiNetwork(context)
        val cellularNetwork = getCellularNetwork(context)

        if (wifiNetwork != null) {
            cm.bindProcessToNetwork(wifiNetwork)
            Log.d("WebRTC", "✅ Wi-Fi 네트워크로 바인딩 완료")

            // ✅ WebSocket 연결 (Wi-Fi 기반)
            val client = OkHttpClient.Builder()
                .socketFactory(wifiNetwork.socketFactory)
                .build()
        }

        // ✅ WebRTC 연결 이후 → 셀룰러로 복원
        delay(8000L)

        if (cellularNetwork != null) {
            cm.bindProcessToNetwork(cellularNetwork)
            Log.d("WebRTC", "🔁 셀룰러 네트워크로 복원 완료")
        } else {
            cm.bindProcessToNetwork(null)
            Log.w("WebRTC", "⚠️ 셀룰러 네트워크를 찾지 못해 기본으로 복원")
        }
    }

    AndroidView(
        factory = {
            WebView(it).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        Log.d("WebViewConsole", consoleMessage.message())
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("WebView", "✅ 페이지 로딩 완료: $url")
                    }
                }

                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.clearCache(true)
            webView.removeAllViews()
            webView.destroy()
        }
    )
}


fun getWifiNetwork(context: Context): Network? {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.allNetworks.firstOrNull { network ->
        val caps = cm.getNetworkCapabilities(network)
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }
}


fun getCellularNetwork(context: Context): Network? {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.allNetworks.firstOrNull { network ->
        val caps = cm.getNetworkCapabilities(network)
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }
}