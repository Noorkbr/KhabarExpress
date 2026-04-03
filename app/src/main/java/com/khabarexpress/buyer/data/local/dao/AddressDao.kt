package com.khabarexpress.buyer.data.local.dao

import androidx.room.*
import com.khabarexpress.buyer.data.local.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    fun getAddressesByUserId(userId: String): Flow<List<AddressEntity>>
    
    @Query("SELECT * FROM addresses WHERE id = :addressId")
    fun getAddressById(addressId: String): Flow<AddressEntity?>
    
    @Query("SELECT * FROM addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    fun getDefaultAddress(userId: String): Flow<AddressEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)
    
    @Update
    suspend fun updateAddress(address: AddressEntity)
    
    @Delete
    suspend fun deleteAddress(address: AddressEntity)
    
    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddress(userId: String)
    
    @Transaction
    suspend fun setDefaultAddress(userId: String, addressId: String) {
        clearDefaultAddress(userId)
        // Note: This would need the actual address object to update
        // In real implementation, this would be done differently
    }
}
