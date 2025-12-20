package com.keremsen.e_commerce.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.keremsen.e_commerce.api.*
import com.keremsen.e_commerce.data.local.DataStoreManager
import com.keremsen.e_commerce.data.remote.*
import com.keremsen.e_commerce.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = Constants.BASE_URL

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideDataStoreManager(dataStore: DataStore<Preferences>): DataStoreManager = DataStoreManager(dataStore)

    @Provides
    @Singleton
    fun provideAuthInterceptor(dataStoreManager: DataStoreManager): AuthInterceptor = AuthInterceptor(dataStoreManager)

    @Provides
    @Singleton
    fun provideAuthAuthenticator(
        dataStoreManager: DataStoreManager,
        authApiService: javax.inject.Provider<AuthApiService>
    ): AuthAuthenticator = AuthAuthenticator(dataStoreManager, authApiService)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authAuthenticator: AuthAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- API SERVİSLERİ ---

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideProductService(retrofit: Retrofit): ProductApiService = retrofit.create(ProductApiService::class.java)

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserApiService = retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideOrderService(retrofit: Retrofit): OrderApiService = retrofit.create(OrderApiService::class.java)

    @Provides
    @Singleton
    fun provideCartService(retrofit: Retrofit): CartApiService = retrofit.create(CartApiService::class.java)

    @Provides
    @Singleton
    fun provideCategoryService(retrofit: Retrofit): CategoryApiService = retrofit.create(CategoryApiService::class.java)

    @Provides
    @Singleton
    fun provideFavoriteApiService(retrofit: Retrofit): FavoriteApiService { return retrofit.create(FavoriteApiService::class.java) }

    @Provides
    @Singleton
    fun provideAddressService(retrofit: Retrofit): AddressApiService = retrofit.create(AddressApiService::class.java)
}