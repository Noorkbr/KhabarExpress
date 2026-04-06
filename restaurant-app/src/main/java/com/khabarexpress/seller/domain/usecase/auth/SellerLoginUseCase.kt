package com.khabarexpress.seller.domain.usecase.auth

import com.khabarexpress.seller.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase for authenticating a seller/restaurant owner.
 *
 * @param phone    Registered phone number.
 * @param password Account password.
 * @return [Result] wrapping [Unit] on success.
 */
class SellerLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, password: String): Result<Unit> =
        authRepository.login(phone, password)
}
