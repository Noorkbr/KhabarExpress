package com.khabarexpress.buyer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KhabarExpressApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // If initialization fails, check:
    }
}
