package com.khabarexpress.seller.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.khabarexpress.seller.presentation.auth.RestaurantLoginScreen
import com.khabarexpress.seller.presentation.dashboard.DashboardScreen

sealed class RestaurantScreen(val route: String) {
    object Dashboard : RestaurantScreen("dashboard")
    object Login : RestaurantScreen("login")
    object Orders : RestaurantScreen("orders")
    object OrderDetails : RestaurantScreen("order_details/{orderId}") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
    object Menu : RestaurantScreen("menu")
    object AddEditItem : RestaurantScreen("add_edit_item/{itemId}") {
        fun createRoute(itemId: String?) = if (itemId != null) "add_edit_item/$itemId" else "add_edit_item/new"
    }
    object Analytics : RestaurantScreen("analytics")
    object Profile : RestaurantScreen("profile")
}

private const val TRANSITION_DURATION = 350

@Composable
fun RestaurantNavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = RestaurantScreen.Login.route,
        enterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION),
                    initialOffsetX = { it / 4 }
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION),
                    targetOffsetX = { -it / 4 }
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION),
                    initialOffsetX = { -it / 4 }
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION),
                    targetOffsetX = { it / 4 }
                )
        }
    ) {
        composable(
            route = RestaurantScreen.Login.route,
            enterTransition = { fadeIn(tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) }
        ) {
            RestaurantLoginScreen(navController = navController)
        }

        composable(RestaurantScreen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        // Add other screens as they are implemented
    }
}
