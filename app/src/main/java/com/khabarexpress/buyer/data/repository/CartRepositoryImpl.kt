package com.khabarexpress.buyer.data.repository

import com.khabarexpress.buyer.data.local.dao.CartDao
import com.khabarexpress.buyer.data.local.entity.CartItemEntity
import com.khabarexpress.buyer.domain.model.*
import com.khabarexpress.buyer.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CartRepository
 * Handles cart operations using Room database for local persistence
 * Provides reactive cart updates via Flow
 */
@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    // Constants for cart calculations
    private val TAX_RATE = 0.05 // 5% tax
    private val DELIVERY_FEE = 30.0 // Fixed delivery fee in BDT

    /**
     * Get current cart as Flow
     */
    override fun getCart(): Flow<Cart?> {
        return cartDao.getAllCartItems().map { entities ->
            if (entities.isEmpty()) {
                null
            } else {
                val restaurantId = entities.firstOrNull()?.restaurantId ?: return@map null
                
                val cartItems = entities.map { entity ->
                    val customizations = try {
                        if (entity.customizations.isNullOrEmpty()) {
                            emptyList()
                        } else {
                            Json.decodeFromString<List<SelectedCustomization>>(entity.customizations)
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    CartItem(
                        id = entity.id.toString(),
                        menuItem = MenuItem(
                            id = entity.menuItemId,
                            name = entity.name,
                            description = entity.description,
                            price = entity.price,
                            imageUrl = entity.imageUrl,
                            isVegetarian = false,
                            isVegan = false,
                            isGlutenFree = false,
                            customizations = emptyList(),
                            isAvailable = true,
                            rating = 0.0,
                            totalOrders = 0
                        ),
                        quantity = entity.quantity,
                        selectedCustomizations = customizations,
                        specialInstructions = null
                    )
                }
                
                Cart(
                    restaurantId = restaurantId,
                    items = cartItems,
                    promoCode = null // Promo code would be stored separately
                )
            }
        }
    }

    /**
     * Add item to cart
     */
    override suspend fun addItem(item: CartItem): Result<Unit> {
        return try {
            // Check if cart has items from different restaurant
            val existingItems = mutableListOf<CartItemEntity>()
            cartDao.getAllCartItems().collect { items ->
                existingItems.addAll(items)
            }
            
            // If cart has items and restaurant is different, return error
            if (existingItems.isNotEmpty()) {
                val existingRestaurantId = existingItems.first().restaurantId
                if (item.restaurantId.isNotEmpty() && existingRestaurantId.isNotEmpty() 
                    && existingRestaurantId != item.restaurantId) {
                    return Result.failure(Exception(
                        "Your cart contains items from another restaurant. Clear your cart to add items from this restaurant."
                    ))
                }
            }
            
            // Serialize customizations
            val customizationsJson = if (item.selectedCustomizations.isNotEmpty()) {
                Json.encodeToString(item.selectedCustomizations)
            } else {
                null
            }
            
            val entity = CartItemEntity(
                id = 0, // Auto-generate
                restaurantId = item.restaurantId,
                restaurantName = item.restaurantName,
                menuItemId = item.menuItem.id,
                name = item.menuItem.name,
                description = item.menuItem.description,
                price = item.menuItem.price,
                imageUrl = item.menuItem.imageUrl,
                quantity = item.quantity,
                customizations = customizationsJson,
                addedAt = System.currentTimeMillis()
            )
            
            cartDao.insertCartItem(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add item to cart: ${e.message}", e))
        }
    }

    /**
     * Update item quantity
     */
    override suspend fun updateQuantity(itemId: String, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                // Remove item if quantity is 0 or negative
                removeItem(itemId)
            } else {
                cartDao.updateQuantity(itemId.toLong(), quantity)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update quantity: ${e.message}", e))
        }
    }

    /**
     * Remove item from cart
     */
    override suspend fun removeItem(itemId: String): Result<Unit> {
        return try {
            val entity = CartItemEntity(
                id = itemId.toLong(),
                restaurantId = "",
                restaurantName = "",
                menuItemId = "",
                name = "",
                description = "",
                price = 0.0,
                imageUrl = null,
                quantity = 0,
                customizations = null,
                addedAt = 0L
            )
            cartDao.deleteCartItem(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to remove item: ${e.message}", e))
        }
    }

    /**
     * Clear entire cart
     */
    override suspend fun clearCart(): Result<Unit> {
        return try {
            cartDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to clear cart: ${e.message}", e))
        }
    }

    /**
     * Apply promo code
     */
    override suspend fun applyPromoCode(code: String): Result<PromoCode> {
        return try {
            // TODO: Call API to validate promo code
            // For now, return a mock promo code
            val promoCode = PromoCode(
                code = code,
                description = "Sample Promo",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10.0,
                minOrderAmount = 100.0,
                maxDiscount = 50.0,
                validUntil = System.currentTimeMillis() + 86400000, // 24 hours
                isActive = true
            )
            Result.success(promoCode)
        } catch (e: Exception) {
            Result.failure(Exception("Invalid promo code: ${e.message}", e))
        }
    }

    /**
     * Remove promo code
     */
    override suspend fun removePromoCode(): Result<Unit> {
        return try {
            // TODO: Remove promo code from cart state
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to remove promo code: ${e.message}", e))
        }
    }

    /**
     * Validate promo code
     */
    override suspend fun validatePromoCode(code: String, orderAmount: Double): Result<PromoCode> {
        return try {
            // TODO: Call API to validate promo code with order amount
            val promoCode = PromoCode(
                code = code,
                description = "Sample Promo",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10.0,
                minOrderAmount = 100.0,
                maxDiscount = 50.0,
                validUntil = System.currentTimeMillis() + 86400000,
                isActive = true
            )
            
            if (orderAmount < promoCode.minOrderAmount) {
                Result.failure(Exception("Minimum order amount not met"))
            } else {
                Result.success(promoCode)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to validate promo code: ${e.message}", e))
        }
    }
}
