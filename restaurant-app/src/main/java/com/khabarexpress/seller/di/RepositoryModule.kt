package com.khabarexpress.seller.di

import android.content.Context
import com.khabarexpress.seller.data.local.preferences.AppPreferences
import com.khabarexpress.seller.data.repository.AuthRepositoryImpl
import com.khabarexpress.seller.data.repository.MenuRepositoryImpl
import com.khabarexpress.seller.data.repository.OrderRepositoryImpl
import com.khabarexpress.seller.data.repository.RestaurantRepositoryImpl
import com.khabarexpress.seller.domain.repository.AuthRepository
import com.khabarexpress.seller.domain.repository.MenuRepository
import com.khabarexpress.seller.domain.repository.OrderRepository
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(impl: RestaurantRepositoryImpl): RestaurantRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindMenuRepository(impl: MenuRepositoryImpl): MenuRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }
}
