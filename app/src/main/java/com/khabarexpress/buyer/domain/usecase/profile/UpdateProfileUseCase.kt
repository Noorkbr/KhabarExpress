package com.khabarexpress.buyer.domain.usecase.profile

import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase for updating the current user's profile information.
 *
 * @param name     Optional new display name.
 * @param phone    Optional new phone number.
 * @param profileImageUrl Optional new profile image URL.
 * @return [Result] wrapping the updated [User] on success.
 */
class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String? = null,
        phone: String? = null,
        profileImageUrl: String? = null
    ): Result<User> = userRepository.updateProfile(name, phone, profileImageUrl)
}
