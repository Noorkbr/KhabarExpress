package com.khabarexpress.buyer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.khabarexpress.buyer.data.local.dao.*
import com.khabarexpress.buyer.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        AddressEntity::class,
        CartItemEntity::class,
        FavoriteEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KhabarExpressDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun addressDao(): AddressDao
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentSearchDao(): RecentSearchDao
}
