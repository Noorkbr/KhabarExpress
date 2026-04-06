package com.khabarexpress.buyer.domain.usecase.profile

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase for adding a new delivery address to the current user's account.
 *
 * @param address The [Address] to persist.
 * @return [Result] wrapping [Unit] on success.
 */
class AddAddressUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(address: Address): Result<Unit> =
        userRepository.addAddress(address)
}
