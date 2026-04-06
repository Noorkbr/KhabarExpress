package com.khabarexpress.seller.domain.usecase.dashboard

import com.khabarexpress.seller.domain.model.Analytics
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * UseCase for fetching earnings/analytics data for a given time period.
 *
 * @param period Time period key: "today", "week", or "month".
 * @return [Result] wrapping [Analytics] on success.
 */
class GetEarningsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(period: String = "today"): Result<Analytics> =
        restaurantRepository.getAnalytics(period)
}
