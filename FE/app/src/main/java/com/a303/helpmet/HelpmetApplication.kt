package com.a303.helpmet

import android.app.Application
import com.a303.helpmet.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HelpmetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Android Context
            androidContext(this@HelpmetApplication)
            modules(appModule)
        }
    }
}