package com.khabarexpress.buyer.domain.usecase.profile

import com.khabarexpress.buyer.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase for permanently removing a saved delivery address.
 *
 * @param addressId Unique identifier of the address to delete.
 * @return [Result] wrapping [Unit] on success.
 */
class DeleteAddressUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(addressId: String): Result<Unit> =
        userRepository.deleteAddress(addressId)
}
