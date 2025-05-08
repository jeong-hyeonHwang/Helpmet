package com.a303.helpmet.di

import com.a303.helpmet.data.network.RetrofitClient
import com.a303.helpmet.data.network.interceptor.ErrorInterceptor
import com.a303.helpmet.data.service.FakeNavigationService
import com.a303.helpmet.data.service.NavigationService
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import org.koin.core.qualifier.named
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

val networkModule = module {
    // Json 파서
    single { Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    } }

    // 로깅 인터셉터
    single { HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    } }


    // 에러 처리 인터셉터
    single<Interceptor>(named("error")) {
        ErrorInterceptor()
    }

    // RetrofitClient 자체를 single로 등록
    single { RetrofitClient }

    // NavigationService를 RetrofitClient에서 꺼내 등록
    single<NavigationService> {
        get<RetrofitClient>().navigationService
    }
}

val fakeNetworkModule = module {
    single<NavigationService> {
        // Json 설정은 networkModule과 동일하게
        FakeNavigationService()
    }
}
