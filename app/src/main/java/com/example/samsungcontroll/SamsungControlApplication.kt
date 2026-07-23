package com.example.samsungcontroll

import android.app.Application
import com.example.samsungcontroll.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SamsungControlApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SamsungControlApplication)
            modules(appModule)
        }
    }
}
