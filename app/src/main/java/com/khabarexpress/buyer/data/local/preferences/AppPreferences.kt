package com.khabarexpress.buyer.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khabar_lagbe_prefs")

/**
 * DataStore manager for storing app preferences
 * Handles authentication tokens, user settings, and other persistent data
 */
class AppPreferences(private val context: Context) {
    
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
    }
    
    /**
     * Save authentication token
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }
    
    /**
     * Get authentication token as Flow
     */
    fun getAuthToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN]
        }
    }
    
    /**
     * Get authentication token synchronously
     */
    suspend fun getAuthTokenSync(): String? {
        var token: String? = null
        context.dataStore.edit { preferences ->
            token = preferences[AUTH_TOKEN]
        }
        return token
    }
    
    /**
     * Save refresh token
     */
    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN] = token
        }
    }
    
    /**
     * Get refresh token
     */
    suspend fun getRefreshToken(): String? {
        var token: String? = null
        context.dataStore.edit { preferences ->
            token = preferences[REFRESH_TOKEN]
        }
        return token
    }
    
    /**
     * Save user ID
     */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }
    
    /**
     * Get user ID
     */
    suspend fun getUserId(): String? {
        var userId: String? = null
        context.dataStore.edit { preferences ->
            userId = preferences[USER_ID]
        }
        return userId
    }
    
    /**
     * Clear all authentication data
     */
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(USER_ID)
        }
    }
}
