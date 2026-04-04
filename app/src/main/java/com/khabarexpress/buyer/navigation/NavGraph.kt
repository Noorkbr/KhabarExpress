package com.khabarexpress.buyer.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.khabarexpress.buyer.presentation.auth.login.LoginScreen
import com.khabarexpress.buyer.presentation.auth.register.RegisterScreen
import com.khabarexpress.buyer.presentation.auth.otp.OTPVerificationScreen
import com.khabarexpress.buyer.presentation.onboarding.OnboardingScreen
import com.khabarexpress.buyer.presentation.splash.SplashScreen
import com.khabarexpress.buyer.presentation.home.HomeScreen
import com.khabarexpress.buyer.presentation.restaurant.RestaurantDetailsScreen
import com.khabarexpress.buyer.presentation.cart.CartScreen
import com.khabarexpress.buyer.presentation.checkout.CheckoutScreen
import com.khabarexpress.buyer.presentation.checkout.payment.PaymentMethodScreen
import com.khabarexpress.buyer.presentation.checkout.address.AddressSelectionScreen
import com.khabarexpress.buyer.presentation.checkout.address.AddAddressScreen
import com.khabarexpress.buyer.presentation.order.tracking.OrderTrackingScreen
import com.khabarexpress.buyer.presentation.order.history.OrderHistoryScreen
import com.khabarexpress.buyer.presentation.order.details.OrderDetailsScreen
import com.khabarexpress.buyer.presentation.profile.ProfileScreen
import com.khabarexpress.buyer.presentation.profile.edit.EditProfileScreen
import com.khabarexpress.buyer.presentation.profile.addresses.AddressManagementScreen
import com.khabarexpress.buyer.presentation.profile.favorites.FavoritesScreen
import com.khabarexpress.buyer.presentation.search.SearchScreen
import com.khabarexpress.buyer.presentation.admin.dashboard.AdminDashboardScreen
import com.khabarexpress.buyer.presentation.admin.verification.RestaurantVerificationScreen
import com.khabarexpress.buyer.presentation.admin.reports.PaymentReportsScreen

private const val TRANSITION_DURATION = 350

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
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
        // Splash – no transition, instant
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) }
        ) {
            SplashScreen(navController = navController)
        }

        // Onboarding
        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) }
        ) {
            OnboardingScreen(navController = navController)
        }
        
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(
            route = Screen.OTPVerification.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OTPVerificationScreen(
                phoneNumber = phoneNumber,
                navController = navController
            )
        }
        
        // Home
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        // Search
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        
        // Restaurant
        composable(
            route = Screen.RestaurantDetails.route,
            arguments = listOf(
                navArgument("restaurantId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable
            RestaurantDetailsScreen(
                restaurantId = restaurantId,
                navController = navController
            )
        }
        
        // Cart
        composable(Screen.Cart.route) {
            CartScreen(navController = navController)
        }
        
        // Checkout
        composable(Screen.Checkout.route) {
            CheckoutScreen(navController = navController)
        }
        
        composable(Screen.PaymentMethod.route) {
            PaymentMethodScreen(navController = navController)
        }
        
        composable(Screen.AddressSelection.route) {
            AddressSelectionScreen(navController = navController)
        }
        
        composable(Screen.AddAddress.route) {
            AddAddressScreen(navController = navController)
        }
        
        composable(
            route = Screen.EditAddress.route,
            arguments = listOf(
                navArgument("addressId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getString("addressId")
            AddAddressScreen(
                navController = navController,
                addressId = addressId
            )
        }
        
        // Order
        composable(
            route = Screen.OrderTracking.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderTrackingScreen(
                orderId = orderId,
                navController = navController
            )
        }
        
        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(navController = navController)
        }
        
        composable(
            route = Screen.OrderDetails.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderDetailsScreen(
                orderId = orderId,
                navController = navController
            )
        }
        
        // Profile
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        
        composable(Screen.SavedAddresses.route) {
            AddressManagementScreen(navController = navController)
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }
        
        // Admin screens
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
        
        composable(Screen.AdminRestaurantVerification.route) {
            RestaurantVerificationScreen(navController = navController)
        }
        
        composable(Screen.AdminPaymentReports.route) {
            PaymentReportsScreen(navController = navController)
        }
    }
}
