package com.a303.helpmet.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.DnsResolver
import android.net.Network
import android.util.Log
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.data.service.DeviceService
import com.a303.helpmet.data.service.NavigationService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class RetrofitProvider(
    private val context: Context, // ✅ context를 생성자에서 받는다
    private val jsonParser: Json,
    private val loggingInterceptor: HttpLoggingInterceptor,
    private val errorInterceptor: Interceptor
) {
    private val defaultOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(errorInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    private val defaultRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_API_HOST)
            .client(defaultOkHttpClient)
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val navigationService: NavigationService by lazy {
        defaultRetrofit.create(NavigationService::class.java)
    }

    val deviceService: DeviceService by lazy {
        defaultRetrofit.create(DeviceService::class.java)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createRetrofitForNetwork(network: Network): Retrofit {
        val socketFactory = network.socketFactory

        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

        val networkOkHttpClient = OkHttpClient.Builder()
            .socketFactory(socketFactory)
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    return try {
                        val linkProperties = connectivityManager.getLinkProperties(network)
                        val dnsServers = linkProperties?.dnsServers

                        if (dnsServers.isNullOrEmpty()) {
                            Log.w("RetrofitProvider", "⚠️ 셀룰러 DNS 서버 정보 없음 → 기본 DNS 사용")
                            InetAddress.getAllByName(hostname).toList()
                        } else {
                            // OkHttp는 별도 DNS 주소 직접 사용은 지원하지 않음
                            InetAddress.getAllByName(hostname).toList()
                        }
                    } catch (e: Exception) {
                        Log.e("RetrofitProvider", "❌ DNS lookup 실패: ${e.message}")
                        emptyList()
                    }
                }
            })
            .addInterceptor(loggingInterceptor)
            .addInterceptor(errorInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_API_HOST)
            .client(networkOkHttpClient)
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    fun createNavigationServiceForNetwork(network: Network): NavigationService {
        return createRetrofitForNetwork(network).create(NavigationService::class.java)
    }

    fun createDeviceServiceForNetwork(network: Network): DeviceService {
        return createRetrofitForNetwork(network).create(DeviceService::class.java)
    }
}
