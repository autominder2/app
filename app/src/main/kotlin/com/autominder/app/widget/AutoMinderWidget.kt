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
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.ui.util.DateFormatUtil
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun reminderDao(): ReminderDao
    fun vehicleDao(): VehicleDao
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

        val urgentReminder = try { reminderDao.getMostUrgentReminder() } catch (_: Exception) { null }
        val overdueCount = try { reminderDao.getOverdueCount() } catch (_: Exception) { 0 }
        val dueSoonCount = try { reminderDao.getDueSoonCount() } catch (_: Exception) { 0 }
        val vehicleCount = try { vehicleDao.getActiveVehicleCount() } catch (_: Exception) { 0 }

        val healthScore = (100 - (25 * overdueCount) - (10 * dueSoonCount)).coerceIn(0, 100)
        val serviceLabel = urgentReminder?.let { it.customLabel ?: it.serviceType.label }
        val dueDateLabel = urgentReminder?.nextDueDate?.let { DateFormatUtil.formatDate(it) }

        provideContent {
            GlanceTheme {
                WidgetContent(
                    healthScore = healthScore,
                    serviceLabel = serviceLabel,
                    dueDateLabel = dueDateLabel,
                    vehicleCount = vehicleCount,
                    overdueCount = overdueCount,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    healthScore: Int,
    serviceLabel: String?,
    dueDateLabel: String?,
    vehicleCount: Int,
    overdueCount: Int,
    context: Context
) {
    val size = LocalSize.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            size.width < 200.dp -> SmallWidget(healthScore, context)
            size.height < 150.dp -> MediumWidget(healthScore, serviceLabel, dueDateLabel, context)
            else -> LargeWidget(healthScore, serviceLabel, dueDateLabel, vehicleCount, overdueCount, context)
        }
    }
}

@Composable
private fun SmallWidget(healthScore: Int, context: Context) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = healthScore.toString(),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = context.getString(R.string.widget_health_score),
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun MediumWidget(
    healthScore: Int,
    serviceLabel: String?,
    dueDateLabel: String?,
    context: Context
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = healthScore.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = context.getString(R.string.widget_health_score),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(GlanceModifier.width(16.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = context.getString(R.string.widget_next_service),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = serviceLabel ?: context.getString(R.string.widget_all_caught_up),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            if (dueDateLabel != null) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = context.getString(R.string.widget_due, dueDateLabel),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun LargeWidget(
    healthScore: Int,
    serviceLabel: String?,
    dueDateLabel: String?,
    vehicleCount: Int,
    overdueCount: Int,
    context: Context
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = healthScore.toString(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = context.getString(R.string.widget_health_score),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(GlanceModifier.width(16.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.widget_fleet_status),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = context.getString(R.string.widget_vehicles, vehicleCount),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp
                    )
                )
                if (overdueCount > 0) {
                    Text(
                        text = context.getString(R.string.widget_overdue_count, overdueCount),
                        style = TextStyle(
                            color = GlanceTheme.colors.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        Spacer(GlanceModifier.height(12.dp))
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = context.getString(R.string.widget_next_service),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = serviceLabel ?: context.getString(R.string.widget_all_caught_up),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            if (dueDateLabel != null) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = context.getString(R.string.widget_due, dueDateLabel),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

class AutoMinderWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AutoMinderWidget()
}
