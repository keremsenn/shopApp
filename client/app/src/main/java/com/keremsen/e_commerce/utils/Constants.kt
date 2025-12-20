package com.keremsen.e_commerce.utils

object Constants {
    private const val IP_ADDRESS = "10.118.72.84"
    private const val PORT = "5000"

    const val BASE_URL = "http://$IP_ADDRESS:$PORT/"

    fun getImageUrl(url: String?): String {
        return if (url.isNullOrEmpty()) {
            ""
        } else {
            "${BASE_URL}$url"
        }
    }
}