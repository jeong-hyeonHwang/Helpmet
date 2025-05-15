package com.a303.helpmet.di

import com.a303.helpmet.data.network.socket.CommandSocketClient
import com.a303.helpmet.data.repository.WebsocketRepository
import org.koin.dsl.module

val webSocketModule = module {
    single { CommandSocketClient() } // 재사용되므로 싱글톤
    single { WebsocketRepository(get(), get()) }
}