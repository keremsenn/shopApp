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
        private val TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { it[USER_ID_KEY] = userId }
    }

    suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[TOKEN_KEY] }.firstOrNull()
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}

