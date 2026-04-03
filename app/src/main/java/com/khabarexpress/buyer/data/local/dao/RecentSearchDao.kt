package com.khabarexpress.buyer.data.local.dao

import androidx.room.*
import com.khabarexpress.buyer.data.local.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: RecentSearchEntity)
    
    @Delete
    suspend fun deleteSearch(search: RecentSearchEntity)
    
    @Query("DELETE FROM recent_searches")
    suspend fun clearAllSearches()
    
    @Query("DELETE FROM recent_searches WHERE timestamp < :timestamp")
    suspend fun deleteOldSearches(timestamp: Long)
}
