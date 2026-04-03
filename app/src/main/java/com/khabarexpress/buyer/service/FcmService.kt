package com.khabarexpress.buyer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Firebase Cloud Messaging Service
 * 
 * This is a stub implementation. To enable:
 * 1. Add Firebase dependencies in build.gradle.kts
 * 2. Add google-services.json to app/ directory
 * 3. Extend FirebaseMessagingService instead of Service
 * 4. Implement onMessageReceived and onNewToken
 */
class FcmService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FcmService created (stub implementation)")
    }
    
    companion object {
        private const val TAG = "FcmService"
    }
}

