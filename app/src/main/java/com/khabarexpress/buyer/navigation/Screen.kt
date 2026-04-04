package com.khabarexpress.buyer.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object OTPVerification : Screen("otp_verification/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp_verification/$phoneNumber"
    }
    
    // Main
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Cart : Screen("cart")
    data object Profile : Screen("profile")
    
    // Restaurant
    data object RestaurantDetails : Screen("restaurant/{restaurantId}") {
        fun createRoute(restaurantId: String) = "restaurant/$restaurantId"
    }
    
    // Checkout
    data object Checkout : Screen("checkout")
    data object PaymentMethod : Screen("payment_method")
    data object AddressSelection : Screen("address_selection")
    data object AddAddress : Screen("add_address")
    data object EditAddress : Screen("edit_address/{addressId}") {
        fun createRoute(addressId: String) = "edit_address/$addressId"
    }
    
    // Order
    data object OrderTracking : Screen("order/{orderId}/tracking") {
        fun createRoute(orderId: String) = "order/$orderId/tracking"
    }
    data object OrderHistory : Screen("order_history")
    data object OrderDetails : Screen("order/{orderId}/details") {
        fun createRoute(orderId: String) = "order/$orderId/details"
    }
    
    // Profile
    data object EditProfile : Screen("edit_profile")
    data object SavedAddresses : Screen("saved_addresses")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")
}
