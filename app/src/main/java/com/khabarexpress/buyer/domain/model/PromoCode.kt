package com.khabarexpress.buyer.domain.model

data class PromoCode(
    val code: String,
    val description: String,
    val discountType: DiscountType,
    val discountValue: Double, // percentage or fixed amount
    val minOrderAmount: Double,
    val maxDiscount: Double? = null,
    val validUntil: Long,
    val isActive: Boolean = true
) {
    fun calculateDiscount(orderAmount: Double): Double {
        if (!isActive || orderAmount < minOrderAmount) return 0.0
        
        return when (discountType) {
            DiscountType.PERCENTAGE -> {
                val discount = orderAmount * (discountValue / 100)
                maxDiscount?.let { minOf(discount, it) } ?: discount
            }
            DiscountType.FIXED -> discountValue
        }
    }
}

enum class DiscountType {
    PERCENTAGE,
    FIXED
}
