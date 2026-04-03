package com.khabarexpress.buyer.domain.repository

import com.khabarexpress.buyer.domain.model.Cart
import com.khabarexpress.buyer.domain.model.CartItem
import com.khabarexpress.buyer.domain.model.PromoCode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cart operations
 */
interface CartRepository {
    
    /**
     * Get current cart
     */
    fun getCart(): Flow<Cart?>
    
    /**
     * Add item to cart
     */
    suspend fun addItem(item: CartItem): Result<Unit>
    
    /**
     * Update item quantity
     */
    suspend fun updateQuantity(itemId: String, quantity: Int): Result<Unit>
    
    /**
     * Remove item from cart
     */
    suspend fun removeItem(itemId: String): Result<Unit>
    
    /**
     * Clear entire cart
     */
    suspend fun clearCart(): Result<Unit>
    
    /**
     * Apply promo code
     */
    suspend fun applyPromoCode(code: String): Result<PromoCode>
    
    /**
     * Remove promo code
     */
    suspend fun removePromoCode(): Result<Unit>
    
    /**
     * Validate promo code
     */
    suspend fun validatePromoCode(code: String, orderAmount: Double): Result<PromoCode>
}
