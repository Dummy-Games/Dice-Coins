package com.dicsa.flasg

import android.app.Application
import com.onesignal.OneSignal

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        OneSignal.initWithContext(applicationContext)
        OneSignal.setAppId("642d08ce-4222-4b55-a468-d1f7a794e92e")
    }
}