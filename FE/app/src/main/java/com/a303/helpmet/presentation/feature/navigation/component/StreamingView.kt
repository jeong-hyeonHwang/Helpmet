package com.a303.helpmet.presentation.feature.navigation.component

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.data.repository.DeviceRepository
import com.a303.helpmet.data.service.DeviceService
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.handler.getGatewayIp
import org.koin.androidx.compose.koinViewModel

@Composable
fun StreamingView(
    viewModel: NavigationViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val streamingViewHeight = screenWidth * 3 / 4

    val gatewayIp = getGatewayIp(context)
    val webPageUrl = "http://$gatewayIp:${BuildConfig.SOCKET_PORT}/"

    val isValidPi by viewModel.isValidPi.collectAsState()

    LaunchedEffect(webPageUrl) {
        viewModel.validateDevice(webPageUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(streamingViewHeight)
            .background(Color.Red)
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
    AndroidView(
        factory = { context ->
            val webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                    }
                }
                loadUrl(url)
            }

            webView
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
