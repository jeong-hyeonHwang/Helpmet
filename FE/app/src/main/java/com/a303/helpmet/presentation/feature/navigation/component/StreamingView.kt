package com.a303.helpmet.presentation.feature.navigation.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import java.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.handler.getGatewayIp
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.koin.androidx.compose.koinViewModel

@Composable
fun StreamingView(
    navigationViewModel: NavigationViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val streamingViewHeight = screenWidth * 3 / 4

    val isActiveStreamingView by navigationViewModel.isActiveStreamingView.collectAsState()

    val gatewayIp = getGatewayIp(context)
    val webPageUrl = "http://$gatewayIp:${BuildConfig.SOCKET_PORT}/"

    val isValidPi by navigationViewModel.isValidPi.collectAsState()

    LaunchedEffect(webPageUrl) {
        navigationViewModel.validateDevice(webPageUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HelpmetTheme.colors.black1)
            .requiredHeightIn(max = streamingViewHeight)
            .height(if (isActiveStreamingView) streamingViewHeight else 0.dp)

    ) {
        when (isValidPi) {
            null -> {
                Text("라즈베리파이를 찾는 중입니다...", color = HelpmetTheme.colors.primary)
            }

            true -> {
                WebRTCPage(url = webPageUrl)
            }

            false -> {
                Text("헬프멧과 연결되지 않았습니다.", color = HelpmetTheme.colors.primary)
            }
        }
    }
}

@Composable
fun WebRTCPage(url: String) {
    val context = LocalContext.current

    LaunchedEffect(url) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiNetwork = getWifiNetwork(context)
        val cellularNetwork = getCellularNetwork(context)

        if (wifiNetwork != null) {
            cm.bindProcessToNetwork(wifiNetwork)
            Log.d("WebRTC", "✅ Wi-Fi 네트워크로 바인딩 완료")
        } else {
            Log.e("WebRTC", "❌ Wi-Fi 네트워크를 찾을 수 없습니다")
            return@LaunchedEffect
        }

        // WebRTC 연결 직후 5초 후에 셀룰러로 복원
        delay(5000L)

        if (cellularNetwork != null) {
            cm.bindProcessToNetwork(cellularNetwork)
            Log.d("WebRTC", "🔁 셀룰러 네트워크로 복원 완료")
        } else {
            cm.bindProcessToNetwork(null) // 기본 네트워크로
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