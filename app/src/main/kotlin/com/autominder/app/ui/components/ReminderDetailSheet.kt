package com.autominder.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import androidx.compose.runtime.remember
import com.autominder.app.domain.usecase.DuePrediction
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel
import com.autominder.app.ui.util.overdueByText

/**
 * Static consumer-language knowledge for each service type: what the service
 * is, and the FIXD-style "can it wait?" answer. Local content — works offline.
 */
private data class ServiceTypeInfo(
    @StringRes val whatItMeans: Int,
    @StringRes val canItWait: Int
)

private fun ServiceType.info(): ServiceTypeInfo = when (this) {
    ServiceType.OIL_CHANGE -> ServiceTypeInfo(R.string.info_oil_what, R.string.info_oil_wait)
    ServiceType.TIRE_ROTATION -> ServiceTypeInfo(R.string.info_tires_what, R.string.info_tires_wait)
    ServiceType.BRAKE_SERVICE -> ServiceTypeInfo(R.string.info_brakes_what, R.string.info_brakes_wait)
    ServiceType.BATTERY -> ServiceTypeInfo(R.string.info_battery_what, R.string.info_battery_wait)
    ServiceType.AIR_FILTER -> ServiceTypeInfo(R.string.info_airfilter_what, R.string.info_airfilter_wait)
    ServiceType.CABIN_FILTER -> ServiceTypeInfo(R.string.info_cabinfilter_what, R.string.info_cabinfilter_wait)
    ServiceType.TRANSMISSION -> ServiceTypeInfo(R.string.info_transmission_what, R.string.info_transmission_wait)
    ServiceType.COOLANT -> ServiceTypeInfo(R.string.info_coolant_what, R.string.info_coolant_wait)
    ServiceType.SPARK_PLUGS -> ServiceTypeInfo(R.string.info_sparkplugs_what, R.string.info_sparkplugs_wait)
    ServiceType.TIMING_BELT -> ServiceTypeInfo(R.string.info_timingbelt_what, R.string.info_timingbelt_wait)
    ServiceType.WIPER_BLADES -> ServiceTypeInfo(R.string.info_wipers_what, R.string.info_wipers_wait)
    ServiceType.INSURANCE -> ServiceTypeInfo(R.string.info_insurance_what, R.string.info_insurance_wait)
    ServiceType.REGISTRATION -> ServiceTypeInfo(R.string.info_registration_what, R.string.info_registration_wait)
    ServiceType.INSPECTION -> ServiceTypeInfo(R.string.info_inspection_what, R.string.info_inspection_wait)
    ServiceType.EMISSIONS_TEST -> ServiceTypeInfo(R.string.info_emissions_what, R.string.info_emissions_wait)
    ServiceType.CUSTOM -> ServiceTypeInfo(R.string.info_custom_what, R.string.info_custom_wait)
}

/**
 * The consumer-grade issue detail used by the best automotive apps:
 * plain-language title → severity in words → what it means → can it wait? →
 * personalized timing → actions. All content is local and offline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailSheet(
    reminder: Reminder,
    status: ServiceStatus,
    prediction: DuePrediction?,
    currentOdometerKm: Int,
    onMarkComplete: () -> Unit,
    onSnooze: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val info = reminder.serviceType.info()
    val unit = LocalDistanceUnit.current

    val (severityText, severityColor, onSeverityColor) = when (status) {
        ServiceStatus.OVERDUE -> Triple(
            stringResource(R.string.reminder_detail_severity_overdue),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        ServiceStatus.DUE_SOON -> Triple(
            stringResource(R.string.reminder_detail_severity_due_soon),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        ServiceStatus.SNOOZED -> Triple(
            stringResource(R.string.reminder_detail_severity_snoozed),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Triple(
            stringResource(R.string.reminder_detail_severity_ok),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.customLabel ?: reminder.serviceType.localizedLabel(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                SeverityBadge(text = severityText, container = severityColor, content = onSeverityColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personalized timing first — it's the answer users came for.
            SheetSection(title = stringResource(R.string.reminder_detail_timing)) {
                val kmLeft = prediction?.kmRemaining
                val expectedAt = prediction?.predictedAt
                // Same rule as VehicleDetail's ReminderCard: an overdue-by-mileage
                // reminder leads with the mileage story, never a future date.
                val overdueByMileage = status == ServiceStatus.OVERDUE &&
                    reminder.nextDueOdometer != null &&
                    currentOdometerKm >= reminder.nextDueOdometer
                if (overdueByMileage) {
                    Text(
                        text = overdueByText(
                            overdueKm = currentOdometerKm - reminder.nextDueOdometer!!,
                            intervalKm = reminder.intervalKm,
                            distanceUnit = unit
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Original due date demoted to secondary reference context.
                    if (reminder.nextDueDate != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.vehicle_detail_due_date, DateFormatUtil.formatDate(reminder.nextDueDate)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val timing = when {
                        status == ServiceStatus.OVERDUE ->
                            stringResource(R.string.reminder_detail_timing_overdue)
                        kmLeft != null && expectedAt != null ->
                            stringResource(
                                R.string.vehicle_detail_forecast,
                                DistanceFormat.grouped(DistanceUtil.kmToDisplay(kmLeft, unit)),
                                DistanceUtil.unitLabel(unit),
                                DateFormatUtil.formatDate(expectedAt)
                            )
                        expectedAt != null ->
                            stringResource(R.string.vehicle_detail_due_date, DateFormatUtil.formatDate(expectedAt))
                        reminder.nextDueDate != null ->
                            stringResource(R.string.vehicle_detail_due_date, DateFormatUtil.formatDate(reminder.nextDueDate))
                        reminder.nextDueOdometer != null ->
                            stringResource(
                                R.string.vehicle_detail_due_at_dynamic,
                                DistanceFormat.grouped(DistanceUtil.kmToDisplay(reminder.nextDueOdometer, unit)),
                                DistanceUtil.unitLabel(unit)
                            )
                        else -> stringResource(R.string.reminder_detail_timing_unset)
                    }
                    Text(
                        text = timing,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            SheetSection(title = stringResource(R.string.reminder_detail_what_it_means)) {
                Text(
                    text = stringResource(info.whatItMeans),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SheetSection(title = stringResource(R.string.reminder_detail_can_it_wait)) {
                Text(
                    text = stringResource(info.canItWait),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.action_edit))
                }
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalButton(onClick = onSnooze) {
                    Text(stringResource(R.string.action_snooze))
                }
                Button(onClick = onMarkComplete) {
                    Text(stringResource(R.string.action_done))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SeverityBadge(text: String, container: Color, content: Color) {
    Surface(shape = RoundedCornerShape(50), color = container) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SheetSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
