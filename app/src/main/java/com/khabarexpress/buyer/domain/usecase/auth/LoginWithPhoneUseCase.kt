package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Authenticates a buyer user using only their Bangladeshi phone number.
 * No OTP or password is required for buyer app access.
 *
 * @param phone The user's phone number in Bangladeshi format (01XXXXXXXXX).
 * @return [Result] containing the authenticated [User] on success or an error.
 */
class LoginWithPhoneUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<User> =
        authRepository.loginWithPhoneOnly(phone)
}
