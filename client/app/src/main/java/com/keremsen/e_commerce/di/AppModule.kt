package com.keremsen.e_commerce.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.keremsen.e_commerce.api.AddressApiService
import com.keremsen.e_commerce.api.AuthApiService
import com.keremsen.e_commerce.api.CartApiService
import com.keremsen.e_commerce.api.CategoryApiService
import com.keremsen.e_commerce.api.OrderApiService
import com.keremsen.e_commerce.api.ProductApiService
import com.keremsen.e_commerce.api.SellerRequestApiService
import com.keremsen.e_commerce.api.UserApiService
import com.keremsen.e_commerce.data.local.DataStoreManager
import com.keremsen.e_commerce.data.remote.*
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

    // Emülatör kullanıyorsan: "http://10.0.2.2:5000/"
    private const val BASE_URL = "http://192.168.0.3:5000/"

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
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
    fun provideCategoryService(retrofit: Retrofit): CategoryApiService { return retrofit.create(CategoryApiService::class.java) }

    @Provides
    @Singleton
    fun provideSellerRequestService(retrofit: Retrofit): SellerRequestApiService = retrofit.create(SellerRequestApiService::class.java)

    @Provides
    @Singleton
    fun provideAddressService(retrofit: Retrofit): AddressApiService = retrofit.create(AddressApiService::class.java)

}