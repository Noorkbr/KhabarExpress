package com.khabarexpress.buyer.domain.repository

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user profile operations
 */
interface UserRepository {
    
    /**
     * Get current user profile
     */
    fun getUserProfile(): Flow<User?>
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(
        name: String?,
        phone: String?,
        profileImageUrl: String?
    ): Result<User>
    
    /**
     * Get user addresses
     */
    fun getUserAddresses(): Flow<List<Address>>
    
    /**
     * Add new address
     */
    suspend fun addAddress(address: Address): Result<Unit>
    
    /**
     * Update existing address
     */
    suspend fun updateAddress(address: Address): Result<Unit>
    
    /**
     * Delete address
     */
    suspend fun deleteAddress(addressId: String): Result<Unit>
    
    /**
     * Set default address
     */
    suspend fun setDefaultAddress(addressId: String): Result<Unit>
    
    /**
     * Upload profile image
     */
    suspend fun uploadProfileImage(imagePath: String): Result<String>
}
