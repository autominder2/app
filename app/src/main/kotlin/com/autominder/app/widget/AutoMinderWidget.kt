package com.autominder.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.autominder.app.MainActivity
import com.autominder.app.R
import com.autominder.app.data.local.dao.FuelDao
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.ui.util.DateFormatUtil
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun reminderDao(): ReminderDao
    fun vehicleDao(): VehicleDao
    fun fuelDao(): FuelDao
}

class AutoMinderWidget : GlanceAppWidget() {

    companion object {
        private val SMALL = DpSize(120.dp, 48.dp)
        private val MEDIUM = DpSize(250.dp, 110.dp)
        private val LARGE = DpSize(250.dp, 180.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val reminderDao = entryPoint.reminderDao()
        val vehicleDao = entryPoint.vehicleDao()
        val fuelDao = entryPoint.fuelDao()

        val urgentReminder = try { reminderDao.getMostUrgentReminder() } catch (_: Exception) { null }
        val overdueCount = try { reminderDao.getOverdueCount() } catch (_: Exception) { 0 }
        val dueSoonCount = try { reminderDao.getDueSoonCount() } catch (_: Exception) { 0 }
        val vehicleCount = try { vehicleDao.getActiveVehicleCount() } catch (_: Exception) { 0 }
        val primaryVehicle = try { vehicleDao.getPrimaryVehicleOnce() } catch (_: Exception) { null }

        // Compute fuel economy telemetry for primary vehicle if available
        val fuelEntries = if (primaryVehicle != null) {
            try { fuelDao.getFuelEntriesForVehicleOnce(primaryVehicle.id) } catch (_: Exception) { emptyList() }
        } else emptyList()

        val avgEfficiencyText = if (fuelEntries.size >= 2) {
            val sortedAsc = fuelEntries.sortedBy { it.odometer }
            val totalDistance = sortedAsc.last().odometer - sortedAsc.first().odometer
            val totalLiters = fuelEntries.sumOf { it.volumeMilliliters } / 1000.0
            if (totalDistance > 0 && totalLiters > 0) {
                String.format(Locale.getDefault(), "%.1f km/L", totalDistance / totalLiters)
            } else null
        } else null

        val attentionCount = overdueCount + dueSoonCount
        val serviceLabel = urgentReminder?.let { it.customLabel ?: it.serviceType.label }
        val dueDateLabel = urgentReminder?.nextDueDate?.let { DateFormatUtil.formatDate(it) }
        val vehicleName = primaryVehicle?.let { "${it.make} ${it.model}" }
        val vehicleOdo = primaryVehicle?.currentOdometer

        provideContent {
            GlanceTheme {
                WidgetContent(
                    attentionCount = attentionCount,
                    serviceLabel = serviceLabel,
                    dueDateLabel = dueDateLabel,
                    vehicleCount = vehicleCount,
                    overdueCount = overdueCount,
                    vehicleName = vehicleName,
                    vehicleOdometer = vehicleOdo,
                    fuelEconomyText = avgEfficiencyText,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    attentionCount: Int,
    serviceLabel: String?,
    dueDateLabel: String?,
    vehicleCount: Int,
    overdueCount: Int,
    vehicleName: String?,
    vehicleOdometer: Int?,
    fuelEconomyText: String?,
    context: Context
) {
    val size = LocalSize.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            size.width < 200.dp -> SmallWidget(attentionCount, overdueCount, context)
            size.height < 150.dp -> MediumWidget(
                attentionCount = attentionCount,
                overdueCount = overdueCount,
                serviceLabel = serviceLabel,
                dueDateLabel = dueDateLabel,
                vehicleName = vehicleName,
                fuelEconomyText = fuelEconomyText,
                context = context
            )
            else -> LargeWidget(
                attentionCount = attentionCount,
                serviceLabel = serviceLabel,
                dueDateLabel = dueDateLabel,
                vehicleCount = vehicleCount,
                overdueCount = overdueCount,
                vehicleName = vehicleName,
                vehicleOdometer = vehicleOdometer,
                fuelEconomyText = fuelEconomyText,
                context = context
            )
        }
    }
}

@Composable
private fun SmallWidget(attentionCount: Int, overdueCount: Int, context: Context) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (attentionCount > 0) {
            Text(
                text = attentionCount.toString(),
                style = TextStyle(
                    color = if (overdueCount > 0) GlanceTheme.colors.error else GlanceTheme.colors.primary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (overdueCount > 0) {
                    context.getString(R.string.widget_overdue_tag)
                } else {
                    context.resources.getQuantityString(R.plurals.widget_attention_label, attentionCount)
                },
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        } else {
            Text(
                text = "✓",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = context.getString(R.string.widget_all_good_label),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun MediumWidget(
    attentionCount: Int,
    overdueCount: Int,
    serviceLabel: String?,
    dueDateLabel: String?,
    vehicleName: String?,
    fuelEconomyText: String?,
    context: Context
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Attention Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(end = 12.dp)
        ) {
            Text(
                text = attentionCount.toString(),
                style = TextStyle(
                    color = if (overdueCount > 0) GlanceTheme.colors.error else GlanceTheme.colors.primary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = context.resources.getQuantityString(
                    R.plurals.widget_attention_label, attentionCount
                ),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }

        // Right Column: Service & Daily Utility Info
        Column(modifier = GlanceModifier.defaultWeight()) {
            if (vehicleName != null) {
                Text(
                    text = vehicleName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(GlanceModifier.height(2.dp))

            Text(
                text = serviceLabel ?: context.getString(R.string.widget_all_caught_up),
                style = TextStyle(
                    color = if (serviceLabel != null) GlanceTheme.colors.onSurface else GlanceTheme.colors.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            if (dueDateLabel != null) {
                Text(
                    text = context.getString(R.string.widget_due, dueDateLabel),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            } else if (fuelEconomyText != null) {
                Text(
                    text = fuelEconomyText,
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun LargeWidget(
    attentionCount: Int,
    serviceLabel: String?,
    dueDateLabel: String?,
    vehicleCount: Int,
    overdueCount: Int,
    vehicleName: String?,
    vehicleOdometer: Int?,
    fuelEconomyText: String?,
    context: Context
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        // Top Header: Status + Fleet Overview
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = attentionCount.toString(),
                    style = TextStyle(
                        color = if (overdueCount > 0) GlanceTheme.colors.error else GlanceTheme.colors.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = context.resources.getQuantityString(
                        R.plurals.widget_attention_label, attentionCount
                    ),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(GlanceModifier.width(14.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = vehicleName ?: context.getString(R.string.widget_fleet_status),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (vehicleOdometer != null && vehicleOdometer > 0) {
                    Text(
                        text = "$vehicleOdometer km",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                } else {
                    Text(
                        text = context.getString(R.string.widget_vehicles, vehicleCount),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        // Middle Card: Urgent Maintenance Item
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(10.dp)
                .background(GlanceTheme.colors.surfaceVariant)
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = context.getString(R.string.widget_next_service),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = serviceLabel ?: context.getString(R.string.widget_all_caught_up),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (dueDateLabel != null) {
                    Text(
                        text = context.getString(R.string.widget_due, dueDateLabel),
                        style = TextStyle(
                            color = if (overdueCount > 0) GlanceTheme.colors.error else GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(GlanceModifier.height(6.dp))

        // Bottom Telemetry: Fuel Efficiency / Quick Tap
        if (fuelEconomyText != null) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.fuel_avg_economy_label) + ":",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = fuelEconomyText,
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class AutoMinderWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AutoMinderWidget()
}
