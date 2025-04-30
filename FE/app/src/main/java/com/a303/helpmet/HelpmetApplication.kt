package com.a303.helpmet

import android.app.Application
import com.a303.helpmet.di.appModule
import com.kakao.vectormap.KakaoMapSdk
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HelpmetApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // https://apis.map.kakao.com/android_v2/docs/getting-started/quickstart/#2-%EB%84%A4%EC%9D%B4%ED%8B%B0%EB%B8%8C-%EC%95%B1-%ED%82%A4-%EC%B6%94%EA%B0%80
        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAPS_API_KEY)

        startKoin {
            // Android Context
            androidContext(this@HelpmetApplication)
            modules(appModule)
        }
    }
}