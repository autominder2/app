package com.autominder.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    init {
        analyticsHelper.logEvent(AnalyticsEvents.ONBOARDING_STARTED)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setHasSeenOnboarding(true)
            analyticsHelper.logEvent(AnalyticsEvents.ONBOARDING_COMPLETED)
        }
    }
}
