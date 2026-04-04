package com.khabarexpress.buyer.di

import com.khabarexpress.buyer.data.remote.api.AdminApi
import com.khabarexpress.buyer.data.remote.api.AuthApi
import com.khabarexpress.buyer.data.remote.api.OrderApi
import com.khabarexpress.buyer.data.remote.api.RestaurantApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // TODO: Make this configurable via BuildConfig
    // For now using a placeholder. In production, use:
    // BuildConfig.API_BASE_URL or environment-specific configuration
    private const val BASE_URL = "https://api.khabarexpress.com/"
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
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
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideRestaurantApi(retrofit: Retrofit): RestaurantApi {
        return retrofit.create(RestaurantApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideOrderApi(retrofit: Retrofit): OrderApi {
        return retrofit.create(OrderApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAdminApi(retrofit: Retrofit): AdminApi {
        return retrofit.create(AdminApi::class.java)
    }
}
