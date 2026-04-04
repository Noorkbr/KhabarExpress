package com.khabarexpress.seller.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khabar_seller_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val RESTAURANT_ID = stringPreferencesKey("restaurant_id")
        private val RESTAURANT_NAME = stringPreferencesKey("restaurant_name")
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[AUTH_TOKEN] = token }
    }

    fun getAuthToken(): Flow<String?> {
        return context.dataStore.data.map { it[AUTH_TOKEN] }
    }

    suspend fun getAuthTokenSync(): String? {
        return context.dataStore.data.first()[AUTH_TOKEN]
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { it[REFRESH_TOKEN] = token }
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { it[USER_ID] = userId }
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[USER_ID]
    }

    suspend fun saveRestaurantId(restaurantId: String) {
        context.dataStore.edit { it[RESTAURANT_ID] = restaurantId }
    }

    suspend fun getRestaurantId(): String? {
        return context.dataStore.data.first()[RESTAURANT_ID]
    }

    fun getRestaurantIdFlow(): Flow<String?> {
        return context.dataStore.data.map { it[RESTAURANT_ID] }
    }

    suspend fun saveRestaurantName(name: String) {
        context.dataStore.edit { it[RESTAURANT_NAME] = name }
    }

    suspend fun getRestaurantName(): String? {
        return context.dataStore.data.first()[RESTAURANT_NAME]
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(RESTAURANT_ID)
            preferences.remove(RESTAURANT_NAME)
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return getAuthTokenSync() != null
    }
}
