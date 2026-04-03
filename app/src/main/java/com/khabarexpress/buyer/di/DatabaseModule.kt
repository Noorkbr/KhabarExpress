package com.khabarexpress.buyer.di

import android.content.Context
import androidx.room.Room
import com.khabarexpress.buyer.data.local.KhabarExpressDatabase
import com.khabarexpress.buyer.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideKhabarExpressDatabase(@ApplicationContext context: Context): KhabarExpressDatabase {
        return Room.databaseBuilder(
            context,
            KhabarExpressDatabase::class.java,
            "khabarlagbe_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: KhabarExpressDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun provideAddressDao(database: KhabarExpressDatabase): AddressDao {
        return database.addressDao()
    }
    
    @Provides
    @Singleton
    fun provideCartDao(database: KhabarExpressDatabase): CartDao {
        return database.cartDao()
    }
    
    @Provides
    @Singleton
    fun provideFavoriteDao(database: KhabarExpressDatabase): FavoriteDao {
        return database.favoriteDao()
    }
    
    @Provides
    @Singleton
    fun provideRecentSearchDao(database: KhabarExpressDatabase): RecentSearchDao {
        return database.recentSearchDao()
    }
}

