package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Registers a new buyer account.
 * Also provides [verifyOtp] to validate the phone number during registration.
 *
 * @param name     The user's full name.
 * @param email    The user's email address.
 * @param phone    The user's phone number in Bangladeshi format (01XXXXXXXXX).
 * @param password Chosen account password (min 6 characters).
 * @return [Result] containing the newly created [User] on success or an error.
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> = authRepository.register(name, email, phone, password)

    suspend fun verifyOtp(phone: String, otp: String): Result<Boolean> =
        authRepository.verifyOtp(phone, otp)
}
