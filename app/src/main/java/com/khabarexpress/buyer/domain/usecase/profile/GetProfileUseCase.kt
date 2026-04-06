package com.khabarexpress.buyer.domain.usecase.profile

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for retrieving the current user's profile.
 * Exposes a [Flow] that emits the [User] whenever profile data changes.
 */
class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> = userRepository.getUserProfile()
}
