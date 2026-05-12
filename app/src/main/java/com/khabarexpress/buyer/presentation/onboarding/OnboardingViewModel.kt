package com.khabarexpress.buyer.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.data.local.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Onboarding screen.
 * Persists the onboarding-completed flag so users only see onboarding once.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    fun markOnboardingCompleted() {
        viewModelScope.launch {
            appPreferences.saveOnboardingCompleted(true)
        }
    }
}
