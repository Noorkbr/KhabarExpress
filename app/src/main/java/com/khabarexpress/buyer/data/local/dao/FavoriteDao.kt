package com.khabarexpress.buyer.data.local.dao

import androidx.room.*
import com.khabarexpress.buyer.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT * FROM favorites WHERE restaurantId = :restaurantId")
    fun getFavoriteById(restaurantId: String): Flow<FavoriteEntity?>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE restaurantId = :restaurantId)")
    fun isFavorite(restaurantId: String): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE restaurantId = :restaurantId")
    suspend fun deleteFavoriteById(restaurantId: String)
}
