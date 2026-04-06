package com.khabarexpress.seller.domain.usecase.dashboard

import com.khabarexpress.seller.domain.model.Analytics
import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.OrderRepository
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * Data class aggregating all information required to render the dashboard screen.
 *
 * @param analytics     Today's analytics summary.
 * @param pendingOrders Orders awaiting seller action.
 * @param activeOrders  Orders currently being prepared or in transit.
 * @param isOpen        Whether the restaurant is accepting new orders.
 */
data class DashboardData(
    val analytics: Analytics = Analytics(),
    val pendingOrders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val isOpen: Boolean = false
)

/**
 * UseCase for loading all data required by the restaurant dashboard in a single call.
 * Fetches today's analytics, pending orders, and active orders concurrently.
 *
 * @return [Result] wrapping [DashboardData] on success.
 */
class GetDashboardUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(): Result<DashboardData> {
        return try {
            val analyticsResult = restaurantRepository.getAnalytics("today")
            val pendingResult = orderRepository.getOrders(status = "pending")
            val confirmedResult = orderRepository.getOrders(status = "confirmed")
            val preparingResult = orderRepository.getOrders(status = "preparing")
            val profileResult = restaurantRepository.getProfile()

            val analytics = analyticsResult.getOrDefault(Analytics())
            val pending = pendingResult.getOrDefault(emptyList())
            val confirmed = confirmedResult.getOrDefault(emptyList())
            val preparing = preparingResult.getOrDefault(emptyList())
            val isOpen = profileResult.getOrNull()?.isOpen ?: false

            Result.success(
                DashboardData(
                    analytics = analytics,
                    pendingOrders = pending,
                    activeOrders = confirmed + preparing,
                    isOpen = isOpen
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
