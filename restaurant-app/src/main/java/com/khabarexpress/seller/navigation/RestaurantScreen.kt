package com.khabarexpress.seller.navigation

sealed class RestaurantScreen(val route: String) {
    object Login : RestaurantScreen("login")
    object Dashboard : RestaurantScreen("dashboard")
    object Orders : RestaurantScreen("orders")
    object Menu : RestaurantScreen("menu")
    object AddMenuItem : RestaurantScreen("add_menu_item")
    object Earnings : RestaurantScreen("earnings")
    object Settings : RestaurantScreen("settings")
}
