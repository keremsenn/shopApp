package com.keremsen.e_commerce.data.remote

import com.keremsen.e_commerce.data.local.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {

        val request = chain.request()
        val path = request.url.encodedPath

        if (path.contains("login") ||
            path.contains("register") ||
            path.contains("refresh")) {
            return chain.proceed(request)
        }

        val token = runBlocking { dataStoreManager.getAccessToken() }
        val requestBuilder = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}
