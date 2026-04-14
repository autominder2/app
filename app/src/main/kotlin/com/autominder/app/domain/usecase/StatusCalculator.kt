package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.ServiceStatus
import java.util.concurrent.TimeUnit

/**
 * Pure algorithm — zero Android dependencies, fully unit-testable.
 *
 * PRD Section 7.1 invariant:
 *   OVERDUE  is evaluated BEFORE SNOOZED — always.
 *   DUE_SOON window = within 14 days OR within 500 km.
 */
object StatusCalculator {

    private val DUE_SOON_DAYS_THRESHOLD  = 14L
    private val DUE_SOON_KM_THRESHOLD    = 500

    /**
     * @param nowMillis       Current time (injectable for testing)
     * @param currentOdometer Current vehicle odometer (km/mi — unit must be consistent)
     * @param dueDateMillis   When the service is due by date (null if date-based not set)
     * @param dueOdometer     When the service is due by mileage (null if mileage-based not set)
     * @param snoozeUntilMillis When the user snoozed until (null if not snoozed)
     * @param isCompleted     Whether this reminder has been marked complete
     */
    fun calculate(
        nowMillis: Long,
        currentOdometer: Int,
        dueDateMillis: Long?,
        dueOdometer: Int?,
        snoozeUntilMillis: Long?,
        isCompleted: Boolean,
    ): ServiceStatus {
        if (isCompleted) return ServiceStatus.COMPLETED

        // ── 1. OVERDUE — checked BEFORE snooze (business invariant) ──────
        val isOverdueByDate = dueDateMillis != null && nowMillis > dueDateMillis
        val isOverdueByKm   = dueOdometer  != null && currentOdometer > dueOdometer
        if (isOverdueByDate || isOverdueByKm) return ServiceStatus.OVERDUE

        // ── 2. SNOOZED — checked AFTER overdue ───────────────────────────
        if (snoozeUntilMillis != null && nowMillis < snoozeUntilMillis) {
            return ServiceStatus.SNOOZED
        }

        // ── 3. DUE_SOON — within ~14 days or ~500 km ─────────────────────
        val dueSoonThresholdMillis = nowMillis + TimeUnit.DAYS.toMillis(DUE_SOON_DAYS_THRESHOLD)
        val isDueSoonByDate = dueDateMillis != null && dueDateMillis <= dueSoonThresholdMillis
        val isDueSoonByKm   = dueOdometer  != null &&
                (dueOdometer - currentOdometer) <= DUE_SOON_KM_THRESHOLD
        if (isDueSoonByDate || isDueSoonByKm) return ServiceStatus.DUE_SOON

        // ── 4. If any due condition exists but isn't close yet → OK ──────
        if (dueDateMillis != null || dueOdometer != null) return ServiceStatus.OK

        return ServiceStatus.UNKNOWN
    }
}
