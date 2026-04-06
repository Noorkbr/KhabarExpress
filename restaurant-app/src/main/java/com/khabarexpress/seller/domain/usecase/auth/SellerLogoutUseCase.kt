package com.khabarexpress.seller.domain.usecase.auth

import com.khabarexpress.seller.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase for logging out the currently authenticated seller.
 * Clears all locally persisted credentials and session data.
 *
 * @return [Result] wrapping [Unit] on success.
 */
class SellerLogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
