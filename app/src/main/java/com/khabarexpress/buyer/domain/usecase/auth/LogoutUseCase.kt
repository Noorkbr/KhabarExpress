package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Logs out the currently authenticated user and clears the local session.
 *
 * @return [Result] wrapping [Unit] on success or an error.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        authRepository.logout()
}
