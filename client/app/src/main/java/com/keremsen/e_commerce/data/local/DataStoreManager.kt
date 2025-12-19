package com.keremsen.e_commerce.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStoreManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token") // Yeni
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val token: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val refreshToken: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[ACCESS_TOKEN_KEY] = token }
    }

    suspend fun saveRefreshToken(token: String) { // Yeni
        dataStore.edit { it[REFRESH_TOKEN_KEY] = token }
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { it[USER_ID_KEY] = userId }
    }

    suspend fun getAccessToken(): String? = dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()
    suspend fun getRefreshToken(): String? = dataStore.data.map { it[REFRESH_TOKEN_KEY] }.firstOrNull() // Yeni

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}

