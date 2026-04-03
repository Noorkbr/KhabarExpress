package com.khabarexpress.buyer.data.local.dao

import androidx.room.*
import com.khabarexpress.buyer.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>
    
    @Query("SELECT * FROM cart_items WHERE restaurantId = :restaurantId")
    fun getCartItemsByRestaurant(restaurantId: String): Flow<List<CartItemEntity>>
    
    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartItemCount(): Flow<Int>
    
    @Query("SELECT SUM(price * quantity) FROM cart_items")
    fun getCartTotal(): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long
    
    @Update
    suspend fun updateCartItem(item: CartItemEntity)
    
    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)
    
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
    
    @Query("DELETE FROM cart_items WHERE restaurantId = :restaurantId")
    suspend fun clearCartByRestaurant(restaurantId: String)
    
    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Long, quantity: Int)
}
