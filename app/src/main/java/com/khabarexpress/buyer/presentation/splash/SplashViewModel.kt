package com.khabarexpress.buyer.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.data.local.preferences.AppPreferences
import com.khabarexpress.buyer.domain.usecase.auth.CheckAuthStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Splash screen.
 * Determines where to navigate after the splash animation based on:
 *  - Whether the user has already completed onboarding
 *  - Whether the user is already authenticated (has a valid auth token)
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val isAuthenticated = checkAuthStatusUseCase.isAuthenticated()
            val onboardingCompleted = appPreferences.isOnboardingCompleted()

            _destination.value = when {
                isAuthenticated -> SplashDestination.Home
                onboardingCompleted -> SplashDestination.Login
                else -> SplashDestination.Onboarding
            }
        }
    }
}

/** Possible navigation targets after the splash screen. */
sealed class SplashDestination {
    /** User is authenticated — go straight to Home. */
    data object Home : SplashDestination()

    /** Onboarding already seen, but not logged in — go to Login. */
    data object Login : SplashDestination()

    /** First launch — show Onboarding. */
    data object Onboarding : SplashDestination()
}
