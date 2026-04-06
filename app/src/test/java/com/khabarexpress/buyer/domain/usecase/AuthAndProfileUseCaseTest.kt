package com.khabarexpress.buyer.domain.usecase

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import com.khabarexpress.buyer.domain.repository.UserRepository
import com.khabarexpress.buyer.domain.usecase.auth.CheckAuthStatusUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithPhoneUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LogoutUseCase
import com.khabarexpress.buyer.domain.usecase.auth.RegisterUseCase
import com.khabarexpress.buyer.domain.usecase.profile.AddAddressUseCase
import com.khabarexpress.buyer.domain.usecase.profile.DeleteAddressUseCase
import com.khabarexpress.buyer.domain.usecase.profile.GetAddressesUseCase
import com.khabarexpress.buyer.domain.usecase.profile.GetProfileUseCase
import com.khabarexpress.buyer.domain.usecase.profile.SetDefaultAddressUseCase
import com.khabarexpress.buyer.domain.usecase.profile.UpdateAvatarUseCase
import com.khabarexpress.buyer.domain.usecase.profile.UpdateProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginWithPhoneUseCase: LoginWithPhoneUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var checkAuthStatusUseCase: CheckAuthStatusUseCase

    private val testUser = User(
        id = "user-123",
        name = "Test User",
        email = "test@example.com",
        phone = "01712345678"
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        loginWithPhoneUseCase = LoginWithPhoneUseCase(authRepository)
        logoutUseCase = LogoutUseCase(authRepository)
        registerUseCase = RegisterUseCase(authRepository)
        checkAuthStatusUseCase = CheckAuthStatusUseCase(authRepository)
    }

    @Test
    fun `loginWithPhone returns success when credentials are valid`() = runTest {
        coEvery { authRepository.loginWithPhoneOnly("01712345678") } returns Result.success(testUser)

        val result = loginWithPhoneUseCase("01712345678")

        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
        coVerify { authRepository.loginWithPhoneOnly("01712345678") }
    }

    @Test
    fun `loginWithPhone returns failure when phone is invalid`() = runTest {
        val error = Exception("Invalid phone number")
        coEvery { authRepository.loginWithPhoneOnly("invalid") } returns Result.failure(error)

        val result = loginWithPhoneUseCase("invalid")

        assertTrue(result.isFailure)
        assertEquals("Invalid phone number", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout delegates to repository`() = runTest {
        coEvery { authRepository.logout() } returns Result.success(Unit)

        val result = logoutUseCase()

        assertTrue(result.isSuccess)
        coVerify { authRepository.logout() }
    }

    @Test
    fun `logout returns failure on repository error`() = runTest {
        coEvery { authRepository.logout() } returns Result.failure(Exception("Network error"))

        val result = logoutUseCase()

        assertTrue(result.isFailure)
    }
}

class ProfileUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var updateAvatarUseCase: UpdateAvatarUseCase
    private lateinit var getAddressesUseCase: GetAddressesUseCase
    private lateinit var addAddressUseCase: AddAddressUseCase
    private lateinit var deleteAddressUseCase: DeleteAddressUseCase
    private lateinit var setDefaultAddressUseCase: SetDefaultAddressUseCase

    private val testUser = User(
        id = "user-123",
        name = "Test User",
        email = "test@example.com",
        phone = "01712345678"
    )

    private val testAddress = Address(
        id = "addr-1",
        label = "Home",
        houseNo = "10",
        roadNo = "5",
        area = "Gulshan",
        thana = "Gulshan",
        district = "Dhaka",
        division = "Dhaka",
        postalCode = "1212",
        latitude = 23.7945,
        longitude = 90.4044,
        isDefault = true
    )

    @Before
    fun setUp() {
        userRepository = mockk()
        getProfileUseCase = GetProfileUseCase(userRepository)
        updateProfileUseCase = UpdateProfileUseCase(userRepository)
        updateAvatarUseCase = UpdateAvatarUseCase(userRepository)
        getAddressesUseCase = GetAddressesUseCase(userRepository)
        addAddressUseCase = AddAddressUseCase(userRepository)
        deleteAddressUseCase = DeleteAddressUseCase(userRepository)
        setDefaultAddressUseCase = SetDefaultAddressUseCase(userRepository)
    }

    @Test
    fun `getProfile emits user from repository`() = runTest {
        every { userRepository.getUserProfile() } returns flowOf(testUser)

        val result = getProfileUseCase().first()

        assertEquals(testUser, result)
        verify { userRepository.getUserProfile() }
    }

    @Test
    fun `getProfile emits null when user not found`() = runTest {
        every { userRepository.getUserProfile() } returns flowOf(null)

        val result = getProfileUseCase().first()

        assertEquals(null, result)
    }

    @Test
    fun `updateProfile delegates to repository`() = runTest {
        coEvery { userRepository.updateProfile("New Name", null, null) } returns Result.success(testUser)

        val result = updateProfileUseCase(name = "New Name")

        assertTrue(result.isSuccess)
        coVerify { userRepository.updateProfile("New Name", null, null) }
    }

    @Test
    fun `updateAvatar delegates uploadProfileImage to repository`() = runTest {
        val imageUrl = "https://example.com/avatar.jpg"
        coEvery { userRepository.uploadProfileImage("/local/path.jpg") } returns Result.success(imageUrl)

        val result = updateAvatarUseCase("/local/path.jpg")

        assertTrue(result.isSuccess)
        assertEquals(imageUrl, result.getOrNull())
    }

    @Test
    fun `getAddresses emits list from repository`() = runTest {
        val addresses = listOf(testAddress)
        every { userRepository.getUserAddresses() } returns flowOf(addresses)

        val result = getAddressesUseCase().first()

        assertEquals(addresses, result)
    }

    @Test
    fun `addAddress delegates to repository`() = runTest {
        coEvery { userRepository.addAddress(testAddress) } returns Result.success(Unit)

        val result = addAddressUseCase(testAddress)

        assertTrue(result.isSuccess)
        coVerify { userRepository.addAddress(testAddress) }
    }

    @Test
    fun `deleteAddress delegates to repository`() = runTest {
        coEvery { userRepository.deleteAddress("addr-1") } returns Result.success(Unit)

        val result = deleteAddressUseCase("addr-1")

        assertTrue(result.isSuccess)
        coVerify { userRepository.deleteAddress("addr-1") }
    }

    @Test
    fun `setDefaultAddress delegates to repository`() = runTest {
        coEvery { userRepository.setDefaultAddress("addr-1") } returns Result.success(Unit)

        val result = setDefaultAddressUseCase("addr-1")

        assertTrue(result.isSuccess)
        coVerify { userRepository.setDefaultAddress("addr-1") }
    }
}
