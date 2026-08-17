package com.autominder.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.autominder.app.R
import com.autominder.app.domain.util.DistanceUtil

/**
 * How an overdue-by-mileage reminder describes itself.
 *
 * The exact distance is genuinely useful right up to the point where it stops
 * being: a driver who adds a car at 20,000 km and updates the odometer months
 * later at 201,000 km was shown "Overdue by 173,000 km". The arithmetic is
 * correct and the sentence is useless — worse than useless, because a wall of
 * six-figure red readouts reads as a broken app rather than a maintenance
 * backlog, and that impression lands in the first thirty seconds of use.
 *
 * The rule: report the exact distance up to one full service interval past
 * due; beyond that, say what to do instead. One interval is the natural
 * ceiling because it is the largest overshoot that still describes *this*
 * service rather than a missed history of them.
 */
object OverdueCopy {

    /**
     * Whether the precise overdue distance is still worth printing.
     *
     * With no km interval recorded there is no basis for "one interval", so
     * the number stands — inventing a ceiling would be fabricating a value.
     */
    fun showsExactDistance(overdueKm: Int, intervalKm: Int?): Boolean =
        intervalKm == null || intervalKm <= 0 || overdueKm <= intervalKm
}

/**
 * The supporting line for a reminder that is overdue on mileage.
 *
 * Shared by the dashboard attention cards, the vehicle detail rows and the
 * reminder sheet, which previously each built this string themselves — three
 * copies of one rule is three places for it to drift.
 */
@Composable
fun overdueByText(
    overdueKm: Int,
    intervalKm: Int?,
    distanceUnit: String
): String {
    val exact = remember(overdueKm, intervalKm) {
        OverdueCopy.showsExactDistance(overdueKm, intervalKm)
    }
    if (!exact) {
        // The OVERDUE chip, rail and colour already carry the status, so this
        // line carries only the action.
        return stringResource(R.string.reminder_overdue_log_to_reset)
    }
    val formatted = remember(overdueKm, distanceUnit) {
        DistanceFormat.grouped(DistanceUtil.kmToDisplay(overdueKm, distanceUnit))
    }
    return stringResource(
        R.string.vehicle_detail_overdue_by_km,
        formatted,
        DistanceUtil.unitLabel(distanceUnit)
    )
}
