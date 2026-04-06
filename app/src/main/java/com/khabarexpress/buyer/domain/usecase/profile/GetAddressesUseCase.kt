package com.khabarexpress.buyer.domain.usecase.profile

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for retrieving all saved delivery addresses of the current user.
 * Exposes a [Flow] that emits the list of [Address] objects whenever the
 * underlying data changes.
 */
class GetAddressesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<Address>> = userRepository.getUserAddresses()
}
