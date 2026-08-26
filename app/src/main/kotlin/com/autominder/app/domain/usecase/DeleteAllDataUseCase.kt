package com.autominder.app.domain.usecase

import android.content.Context
import com.autominder.app.core.notifications.NotificationHelper
import com.autominder.app.domain.repository.IVehicleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Erases everything the user has recorded, on request.
 *
 * AutoMinder has no account and no server, so "delete my data" cannot mean
 * "ask us to delete it" - it has to happen on the device, immediately and
 * completely. This is the only path that does it.
 *
 * Deleting the vehicles is sufficient for the database: `reminders`,
 * `services`, `mileage_logs` and `fuel_entries` all declare
 * `ForeignKey(onDelete = CASCADE)` on `vehicleId`, so all five tables empty in
 * one statement. Archived vehicles go too.
 *
 * The notification sweep is not optional. Reminders already sitting in the
 * shade outlive the rows they describe, and tapping one would open a detail
 * screen for a vehicle that no longer exists.
 *
 * Periodic WorkManager checks are deliberately left scheduled: with no
 * reminders in the database they find nothing and post nothing, and cancelling
 * them would leave a user who then adds a new vehicle with no background checks
 * until the next app launch.
 *
 * This use case takes a [Context] - unusual for `domain` - because erasing user
 * data has a platform side effect that must succeed or fail together with the
 * database write. Putting the sweep in the ViewModel instead would let the two
 * drift apart.
 */
@Singleton
class DeleteAllDataUseCase @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        vehicleRepository.deleteAllVehicles()
        NotificationHelper.cancelAll(context)
    }
}
