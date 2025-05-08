package com.a303.helpmet.data.network

import com.a303.helpmet.data.service.FakeNavigationService
import com.a303.helpmet.data.service.NavigationService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType
import org.koin.core.qualifier.named
import java.util.concurrent.TimeUnit

object RetrofitClient : KoinComponent {
    // (1) Koin에 등록된 의존성 꺼내기
    private val jsonParser: Json                         by inject()
    private val loggingInterceptor: HttpLoggingInterceptor by inject()
    private val errorInterceptor: Interceptor            by inject(named("error"))

    // (2) OkHttpClient 구성 (inject 한 것들로만)
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(errorInterceptor)
            .build()
    }

    // (3) Retrofit & Service
    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.yourserver.com/")
            .client(okHttpClient)
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val navigationService: NavigationService by lazy {
        retrofit.create(NavigationService::class.java)
    }
}
