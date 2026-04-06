package com.khabarexpress.buyer.domain.usecase.profile

import com.khabarexpress.buyer.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase for marking a specific delivery address as the default.
 * The default address is pre-selected during checkout.
 *
 * @param addressId Unique identifier of the address to set as default.
 * @return [Result] wrapping [Unit] on success.
 */
class SetDefaultAddressUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(addressId: String): Result<Unit> =
        userRepository.setDefaultAddress(addressId)
}
