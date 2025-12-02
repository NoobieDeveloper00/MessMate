package com.kshitiz.messmate

import android.app.Application
import com.kshitiz.messmate.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MessMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MessMateApp)
            modules(appModule)
        }
    }
}