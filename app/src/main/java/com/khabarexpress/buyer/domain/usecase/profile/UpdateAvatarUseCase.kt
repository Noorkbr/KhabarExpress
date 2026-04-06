package com.khabarexpress.buyer.domain.usecase.profile

import com.khabarexpress.buyer.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase for uploading a new avatar image for the current user.
 *
 * @param imagePath Absolute local file path of the selected image.
 * @return [Result] wrapping the remote image URL on success.
 */
class UpdateAvatarUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(imagePath: String): Result<String> =
        userRepository.uploadProfileImage(imagePath)
}
