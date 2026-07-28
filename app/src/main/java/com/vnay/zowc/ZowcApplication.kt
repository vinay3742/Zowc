package com.vnay.zowc

import android.app.Application
import com.vnay.zowc.data.local.ObjectBox
import com.vnay.zowc.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZowcApplication: Application() {
    override fun onCreate(){
        super.onCreate()

        // Initialize ObjectBox
        ObjectBox.init(this)

        // Start Koin Dependency Injection
        startKoin {
            androidContext(this@ZowcApplication)
            modules(appModule)
        }
    }
}