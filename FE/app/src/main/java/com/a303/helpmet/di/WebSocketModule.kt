package com.a303.helpmet.di

import com.a303.helpmet.data.network.socket.DirectionSocketClient
import com.a303.helpmet.data.repository.DirectionSocketRepository
import org.koin.dsl.module

val webSocketModule = module {
    single { DirectionSocketClient() } // 재사용되므로 싱글톤
    single { DirectionSocketRepository(get(), get()) }
}