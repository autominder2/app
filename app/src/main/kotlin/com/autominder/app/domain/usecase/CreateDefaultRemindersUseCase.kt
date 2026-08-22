package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
import javax.inject.Inject

/**
 * One planned line of the seeded maintenance plan — pure data, computed
 * deterministically so the onboarding reveal can show the exact plan that
 * will be saved. This is a GENERAL starting plan from typical intervals,
 * never manufacturer guidance; the UI must say so.
 */
data class PlannedReminder(
    val serviceType: ServiceType,
    val intervalKm: Int,
    val intervalDays: Int,
    /**
     * Null when the vehicle has no odometer reading yet — such a plan is
     * date-driven only. See [CreateDefaultRemindersUseCase.buildPlan].
     */
    val nextDueOdometer: Int?,
    val nextDueDate: Long
)

/**
 * Creates standard maintenance reminders for a newly added vehicle.
 * Expertise: This logic is now intended to be called within a repository transaction.
 *
 * The plan itself is computed by the pure [buildPlan] so the onboarding
 * reveal, the persisted reminders, and the unit tests all share one
 * deterministic source of truth.
 */
class CreateDefaultRemindersUseCase @Inject constructor(
    private val reminderRepository: IReminderRepository
) {

    /** Backwards-compatible entry point (Add Vehicle flow) — TYPICAL driving. */
    suspend operator fun invoke(vehicleId: Long, currentOdometer: Int) {
        invoke(vehicleId, currentOdometer, DrivingAmount.TYPICAL, System.currentTimeMillis())
    }

    suspend operator fun invoke(
        vehicleId: Long,
        currentOdometerKm: Int,
        drivingAmount: DrivingAmount,
        nowMillis: Long
    ) {
        persistPlan(
            vehicleId = vehicleId,
            plan = buildPlan(currentOdometerKm, drivingAmount, nowMillis),
            createdAt = nowMillis
        )
    }

    /** Persists the exact plan previously shown to the user. */
    suspend fun persistPlan(
        vehicleId: Long,
        plan: List<PlannedReminder>,
        createdAt: Long
    ) {
        plan.forEach { planned ->
            reminderRepository.insertReminder(
                Reminder(
                    id = 0,
                    vehicleId = vehicleId,
                    serviceType = planned.serviceType,
                    intervalKm = planned.intervalKm,
                    intervalDays = planned.intervalDays,
                    nextDueOdometer = planned.nextDueOdometer,
                    nextDueDate = planned.nextDueDate,
                    createdAt = createdAt,
                    updatedAt = createdAt
                )
            )
        }
    }

    companion object {
        private const val DAY_MS = 86_400_000L

        private data class ReminderTemplate(
            val serviceType: ServiceType,
            val intervalKm: Int,
            val intervalDays: Int
        )

        private val STANDARD_TEMPLATES = listOf(
            ReminderTemplate(ServiceType.OIL_CHANGE, 8_000, 180),
            ReminderTemplate(ServiceType.TIRE_ROTATION, 12_000, 365),
            ReminderTemplate(ServiceType.AIR_FILTER, 20_000, 365),
            ReminderTemplate(ServiceType.CABIN_FILTER, 20_000, 365),
            ReminderTemplate(ServiceType.BRAKE_SERVICE, 40_000, 730),
            ReminderTemplate(ServiceType.WIPER_BLADES, 15_000, 365),
        )

        // Higher-mileage vehicles get age-appropriate additions. Thresholds
        // are km on the odometer, not vehicle years — mileage is the fact
        // we actually know at onboarding.
        private val COOLANT_TEMPLATE = ReminderTemplate(ServiceType.COOLANT, 40_000, 730)
        private val TRANSMISSION_TEMPLATE = ReminderTemplate(ServiceType.TRANSMISSION, 60_000, 1_460)
        const val COOLANT_FROM_KM = 80_000
        const val TRANSMISSION_FROM_KM = 100_000

        /**
         * Deterministic seeded plan. The km axis is fixed by the template;
         * the DATE axis is the earlier of the template's calendar interval
         * and the time this driver needs to cover the km interval — so a
         * HIGH driver sees sooner dates than a LOW driver for the same
         * service. Sorted soonest-first so index 0 is "what's first".
         *
         * A [currentOdometerKm] of 0 is the app's sentinel for "mileage not
         * added" (`VehicleListScreen`), not a reading of zero. Seeding the km
         * axis from it produced reminders due at 8,000km on a car that had
         * already covered 200,000 — so the moment the driver entered their
         * real mileage, every service was overdue by six figures at once.
         * Without a baseline there is nothing honest to anchor distance to,
         * so the plan is date-driven until a reading exists. The calendar
         * axis still carries it, and logging mileage later fills the rest in.
         */
        fun buildPlan(
            currentOdometerKm: Int,
            drivingAmount: DrivingAmount,
            nowMillis: Long
        ): List<PlannedReminder> {
            val hasOdometerReading = currentOdometerKm > 0
            val templates = buildList {
                addAll(STANDARD_TEMPLATES)
                if (currentOdometerKm >= COOLANT_FROM_KM) add(COOLANT_TEMPLATE)
                if (currentOdometerKm >= TRANSMISSION_FROM_KM) add(TRANSMISSION_TEMPLATE)
            }
            val dailyKm = drivingAmount.annualKm / 365.0
            return templates.map { t ->
                val daysToReachKm = (t.intervalKm / dailyKm).toInt().coerceAtLeast(1)
                val effectiveDays = minOf(t.intervalDays, daysToReachKm)
                PlannedReminder(
                    serviceType = t.serviceType,
                    intervalKm = t.intervalKm,
                    intervalDays = t.intervalDays,
                    nextDueOdometer = if (hasOdometerReading) {
                        currentOdometerKm + t.intervalKm
                    } else {
                        null
                    },
                    nextDueDate = nowMillis + effectiveDays * DAY_MS
                )
            }.sortedWith(compareBy({ it.nextDueDate }, { it.nextDueOdometer ?: Int.MAX_VALUE }))
        }
    }
}
