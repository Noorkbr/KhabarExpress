package com.khabarexpress.seller

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KhabarExpressSellerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize app components
    }
}
