package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.repository.AuthRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> =
        authRepository.sendOtp(phone)
}
