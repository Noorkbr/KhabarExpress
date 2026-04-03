package com.khabarexpress.buyer.data.repository

import com.khabarexpress.buyer.data.local.dao.AddressDao
import com.khabarexpress.buyer.data.local.dao.UserDao
import com.khabarexpress.buyer.data.local.preferences.AppPreferences
import com.khabarexpress.buyer.data.mappers.toDomainModel
import com.khabarexpress.buyer.data.mappers.toEntity
import com.khabarexpress.buyer.data.remote.api.AuthApi
import com.khabarexpress.buyer.data.remote.dto.UpdateProfileRequest
import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository
 * Handles user profile and address management operations
 * Uses Room for local caching and API for remote updates
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val addressDao: AddressDao,
    private val appPreferences: AppPreferences
) : UserRepository {

    /**
     * Get current user profile as Flow
     */
    override fun getUserProfile(): Flow<User?> {
        return appPreferences.getAuthToken().map { token ->
            if (token != null) {
                val userId = appPreferences.getUserId()
                if (userId != null) {
                    var user: User? = null
                    userDao.getUserById(userId).collect { userEntity ->
                        user = userEntity?.toDomainModel()
                    }
                    
                    // Load user's addresses
                    if (user != null) {
                        val addresses = mutableListOf<Address>()
                        addressDao.getAddressesByUserId(userId).collect { addressEntities ->
                            addresses.addAll(addressEntities.map { it.toDomainModel() })
                        }
                        user = user!!.copy(savedAddresses = addresses)
                    }
                    
                    user
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Update user profile
     */
    override suspend fun updateProfile(
        name: String?,
        phone: String?,
        profileImageUrl: String?
    ): Result<User> {
        return try {
            val token = appPreferences.getAuthTokenSync()
                ?: return Result.failure(Exception("Not authenticated"))
            
            val request = UpdateProfileRequest(
                name = name,
                email = null, // Email updates might not be allowed
                phone = phone,
                profileImageUrl = profileImageUrl
            )
            
            val response = authApi.updateProfile("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                
                // Update local cache
                userDao.updateUser(userDto.toEntity())
                
                Result.success(userDto.toDomainModel())
            } else {
                Result.failure(Exception("Failed to update profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Get user addresses as Flow
     */
    override fun getUserAddresses(): Flow<List<Address>> {
        return appPreferences.getAuthToken().map { token ->
            if (token != null) {
                val userId = appPreferences.getUserId()
                if (userId != null) {
                    val addresses = mutableListOf<Address>()
                    addressDao.getAddressesByUserId(userId).collect { addressEntities ->
                        addresses.addAll(addressEntities.map { it.toDomainModel() })
                    }
                    addresses
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    /**
     * Add new address
     */
    override suspend fun addAddress(address: Address): Result<Unit> {
        return try {
            val userId = appPreferences.getUserId()
                ?: return Result.failure(Exception("Not authenticated"))
            
            val entity = address.toEntity(userId)
            
            // If this is set as default, clear other defaults first
            if (address.isDefault) {
                addressDao.clearDefaultAddress(userId)
            }
            
            addressDao.insertAddress(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add address: ${e.message}", e))
        }
    }

    /**
     * Update existing address
     */
    override suspend fun updateAddress(address: Address): Result<Unit> {
        return try {
            val userId = appPreferences.getUserId()
                ?: return Result.failure(Exception("Not authenticated"))
            
            val entity = address.toEntity(userId)
            
            // If this is set as default, clear other defaults first
            if (address.isDefault) {
                addressDao.clearDefaultAddress(userId)
            }
            
            addressDao.updateAddress(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update address: ${e.message}", e))
        }
    }

    /**
     * Delete address
     */
    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        return try {
            val userId = appPreferences.getUserId()
                ?: return Result.failure(Exception("Not authenticated"))
            
            // Get the address to delete
            var addressEntity: com.khabarexpress.buyer.data.local.entity.AddressEntity? = null
            addressDao.getAddressById(addressId).collect { 
                addressEntity = it 
            }
            
            if (addressEntity != null) {
                addressDao.deleteAddress(addressEntity!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Address not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete address: ${e.message}", e))
        }
    }

    /**
     * Set default address
     */
    override suspend fun setDefaultAddress(addressId: String): Result<Unit> {
        return try {
            val userId = appPreferences.getUserId()
                ?: return Result.failure(Exception("Not authenticated"))
            
            // Clear all defaults first
            addressDao.clearDefaultAddress(userId)
            
            // Get the address to update
            var addressEntity: com.khabarexpress.buyer.data.local.entity.AddressEntity? = null
            addressDao.getAddressById(addressId).collect { 
                addressEntity = it 
            }
            
            if (addressEntity != null) {
                // Update the address to set as default
                addressDao.updateAddress(addressEntity!!.copy(isDefault = true))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Address not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to set default address: ${e.message}", e))
        }
    }

    /**
     * Upload profile image
     * This is a stub implementation - actual implementation would handle file upload
     */
    override suspend fun uploadProfileImage(imagePath: String): Result<String> {
        return try {
            // TODO: Implement actual image upload to server
            // For now, return the local path as placeholder
            Result.success(imagePath)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload image: ${e.message}", e))
        }
    }
}
