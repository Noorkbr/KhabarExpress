package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Authenticates a user with email and password (used for staff/restaurant access).
 *
 * @param email    Registered email address.
 * @param password Account password.
 * @return [Result] containing the authenticated [User] on success or an error.
 */
class LoginWithCredentialsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        authRepository.login(email, password)
}
