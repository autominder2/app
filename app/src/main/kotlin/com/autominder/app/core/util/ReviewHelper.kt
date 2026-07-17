package com.autominder.app.core.util

import android.app.Activity
import com.autominder.app.data.local.preferences.UserPreferences
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewHelper @Inject constructor(
    private val userPreferences: UserPreferences
) {
    /**
     * Request an in-app review if the user has reached a certain milestone.
     * Milestone: 3 service logs recorded and hasn't been requested before in this version.
     */
    suspend fun requestReviewIfAppropriate(activity: Activity) {
        val count = userPreferences.serviceLogCount.first()
        val alreadyRequested = userPreferences.hasRequestedReview.first()

        if (count >= 3 && !alreadyRequested) {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()

            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown.
                        // We mark it as requested to avoid nagging the user too often.
                    }
                } else {
                    Timber.w("Review request failed: ${task.exception?.message}")
                }
            }
            // Mark as requested regardless of success to respect Play Store quotas
            // and avoid retrying immediately on failure.
            userPreferences.setHasRequestedReview(true)
        }
    }
}
