package com.khabarexpress.buyer.domain.usecase.admin

import com.khabarexpress.buyer.data.remote.dto.AdminDashboardData
import com.khabarexpress.buyer.domain.repository.AdminRepository
import javax.inject.Inject

/**
 * UseCase for fetching the admin dashboard statistics.
 * Retrieves today's totals, active counters, and pending counts.
 *
 * @return [Result] wrapping [AdminDashboardData] on success.
 */
class GetDashboardStatsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<AdminDashboardData> =
        adminRepository.getDashboardStats()
}
