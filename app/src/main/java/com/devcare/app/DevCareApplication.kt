package com.devcare.app

import android.app.Application
import com.devcare.app.data.AppDatabase
import com.devcare.app.notification.NotificationHelper

class DevCareApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        NotificationHelper.createChannels(this)
    }
}
