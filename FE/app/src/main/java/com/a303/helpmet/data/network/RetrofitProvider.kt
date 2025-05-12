package com.a303.helpmet.data.network

import android.bluetooth.BluetoothClass.Device
import com.a303.helpmet.data.network.adapter.ApiCallAdapterFactory
import com.a303.helpmet.data.service.DeviceService
import com.a303.helpmet.data.service.FakeNavigationService
import com.a303.helpmet.data.service.NavigationService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import java.util.concurrent.TimeUnit

class RetrofitProvider(
    jsonParser: Json,
    loggingInterceptor: HttpLoggingInterceptor,
    errorInterceptor: Interceptor
) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(errorInterceptor)
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.yourserver.com/")
            .client(okHttpClient)
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(ApiCallAdapterFactory())
            .build()
    }

    val navigationService: NavigationService by lazy {
        retrofit.create(NavigationService::class.java)
    }

    val deviceService: DeviceService by lazy {
        retrofit.create(DeviceService::class.java)
    }
}
