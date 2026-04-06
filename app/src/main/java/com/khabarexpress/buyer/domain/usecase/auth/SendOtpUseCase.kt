package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Sends a one-time password (OTP) to the provided phone number via SMS.
 *
 * @param phone The recipient's phone number in Bangladeshi format (01XXXXXXXXX).
 * @return [Result] wrapping [Unit] on success or an error.
 */
class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> =
        authRepository.sendOtp(phone)
}
