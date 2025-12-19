package com.keremsen.e_commerce.data.remote

import com.keremsen.e_commerce.api.AuthApiService
import com.keremsen.e_commerce.data.local.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class AuthAuthenticator @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val authApiService: Provider<AuthApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking { dataStoreManager.getRefreshToken() }

        if (refreshToken.isNullOrBlank()) return null
        val refreshResponse = runBlocking {
            authApiService.get().refreshToken("Bearer $refreshToken")
        }

        return if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
            val newAccessToken = refreshResponse.body()!!.access_token

            runBlocking {
                newAccessToken?.let { dataStoreManager.saveToken(it) }
            }

            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            runBlocking { dataStoreManager.clear() }
            null
        }
    }
}