package com.khabarexpress.buyer.domain.model

data class CartItem(
    val id: String,
    val menuItem: MenuItem,
    val quantity: Int,
    val selectedCustomizations: List<SelectedCustomization> = emptyList(),
    val specialInstructions: String? = null,
    val restaurantId: String = "",
    val restaurantName: String = ""
) {
    val totalPrice: Double
        get() {
            val basePrice = menuItem.price * quantity
            val customizationPrice = selectedCustomizations.sumOf { it.choice.additionalPrice } * quantity
            return basePrice + customizationPrice
        }
}

data class SelectedCustomization(
    val optionId: String,
    val choice: CustomizationChoice
)

data class Cart(
    val restaurantId: String,
    val items: List<CartItem> = emptyList(),
    val promoCode: PromoCode? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }
    
    val discount: Double
        get() = promoCode?.calculateDiscount(subtotal) ?: 0.0
    
    val totalItems: Int
        get() = items.sumOf { it.quantity }
}
