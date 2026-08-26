package com.autominder.app.domain.usecase

import androidx.compose.runtime.Immutable
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What the user actually owns, counted.
 *
 * Every field is a plain count of rows the user created. Nothing here is
 * derived, projected or scored — so there is no way for this card to tell the
 * user something that is not literally true of their own data. That constraint
 * is deliberate: this is the first thing on the Settings screen, and Settings
 * is where trust is either earned or lost.
 */
@Immutable
data class GarageSummary(
    val vehicleCount: Int = 0,
    val serviceCount: Int = 0,
    val reminderCount: Int = 0
) {
    /** True before the user has added anything, so the UI can invite rather than report. */
    val isEmpty: Boolean get() = vehicleCount == 0 && serviceCount == 0 && reminderCount == 0
}

/**
 * Observes the size of the user's garage for the Settings header card.
 *
 * Composed only from cross-vehicle flows that already exist on the repository
 * interfaces — `getAllVehicles`, `getAllServices`, `getAllPendingReminders`.
 * No DAO method, no query and no schema change was added for this, which keeps
 * a presentation feature from reaching into the data layer.
 *
 * Fuel and mileage are deliberately absent: both are exposed per-vehicle only,
 * and summing them would mean either a new DAO query or an N+1 fan-out across
 * vehicles. Showing three honest counts beats showing five at that cost.
 */
@Singleton
class GetGarageSummaryUseCase @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val serviceRepository: IServiceRepository,
    private val reminderRepository: IReminderRepository
) {
    operator fun invoke(): Flow<GarageSummary> = combine(
        vehicleRepository.getAllVehicles(),
        serviceRepository.getAllServices(),
        reminderRepository.getAllPendingReminders()
    ) { vehicles, services, reminders ->
        GarageSummary(
            vehicleCount = vehicles.size,
            serviceCount = services.size,
            reminderCount = reminders.size
        )
    }
}
