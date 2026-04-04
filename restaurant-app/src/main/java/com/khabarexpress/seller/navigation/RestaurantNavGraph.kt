package com.khabarexpress.seller.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.khabarexpress.seller.presentation.auth.RestaurantLoginScreen
import com.khabarexpress.seller.presentation.dashboard.DashboardScreen
import com.khabarexpress.seller.presentation.earnings.EarningsScreen
import com.khabarexpress.seller.presentation.menu.AddMenuItemScreen
import com.khabarexpress.seller.presentation.menu.MenuScreen
import com.khabarexpress.seller.presentation.orders.OrdersScreen
import com.khabarexpress.seller.presentation.settings.SettingsScreen

@Composable
fun RestaurantNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RestaurantScreen.Login.route
    ) {
        composable(RestaurantScreen.Login.route) {
            RestaurantLoginScreen(navController = navController)
        }
        composable(RestaurantScreen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(RestaurantScreen.Orders.route) {
            OrdersScreen(navController = navController)
        }
        composable(RestaurantScreen.Menu.route) {
            MenuScreen(navController = navController)
        }
        composable(RestaurantScreen.AddMenuItem.route) {
            AddMenuItemScreen(navController = navController)
        }
        composable(RestaurantScreen.Earnings.route) {
            EarningsScreen(navController = navController)
        }
        composable(RestaurantScreen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
