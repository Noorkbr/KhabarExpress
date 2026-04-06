package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Authenticates a user by verifying a one-time password (OTP) sent to their phone.
 *
 * @param phone The user's phone number in Bangladeshi format (01XXXXXXXXX).
 * @param otp   The 4–6 digit OTP received via SMS.
 * @return [Result] containing the authenticated [User] on success or an error.
 */
class LoginWithOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, otp: String): Result<User> =
        authRepository.loginWithPhone(phone, otp)
}
