package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

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
