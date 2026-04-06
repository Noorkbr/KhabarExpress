package com.khabarexpress.buyer.domain.usecase.auth

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun isAuthenticated(): Boolean =
        authRepository.isAuthenticated()

    fun getCurrentUser(): Flow<User?> =
        authRepository.getCurrentUser()
}
