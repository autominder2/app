package com.autominder.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.autominder.app.MainActivity
import com.autominder.app.R
import com.autominder.app.core.notifications.NotificationHelper

class AutoMinderWidget : GlanceAppWidget() {

    companion object {
        private val SMALL = DpSize(120.dp, 48.dp)
        private val MEDIUM = DpSize(240.dp, 100.dp)
        private val LARGE = DpSize(240.dp, 170.dp)

        // Semantic Status Colors
        val ColorSuccess = ColorProvider(day = Color(0xFF167A55), night = Color(0xFF34D399))
        val ColorSuccessContainer = ColorProvider(day = Color(0xFFE6F4EA), night = Color(0xFF064E3B))
        val ColorWarning = ColorProvider(day = Color(0xFF9A6700), night = Color(0xFFFBBF24))
        val ColorWarningContainer = ColorProvider(day = Color(0xFFFEF3D6), night = Color(0xFF78350F))
        val ColorError = ColorProvider(day = Color(0xFFB42318), night = Color(0xFFF87171))
        val ColorErrorContainer = ColorProvider(day = Color(0xFFFEE4E2), night = Color(0xFF7F1D1D))
        val ColorPrimaryNavy = ColorProvider(day = Color(0xFF102A56), night = Color(0xFF93C5FD))
        val ColorSurfaceElevated = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1E293B))
        val ColorSurfaceSubtle = ColorProvider(day = Color(0xFFF1F5F9), night = Color(0xFF0F172A))
    }

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetDataProvider.loadWidgetState(context)

        provideContent {
            GlanceTheme {
                WidgetRootContent(state = state, context = context)
            }
        }
    }
}

@Composable
private fun WidgetRootContent(state: AutoMinderWidgetState, context: Context) {
    val size = LocalSize.current

    // Base widget container with launcher-standard rounded corners & background
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(18.dp)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity(createMainIntent(context, state.vehicleId)))
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when (state.stateType) {
            WidgetStateType.LOADING -> WidgetLoadingLayout()
            WidgetStateType.EMPTY -> WidgetEmptyLayout(context)
            WidgetStateType.SETUP_INCOMPLETE -> WidgetSetupIncompleteLayout(state, context)
            WidgetStateType.ERROR -> WidgetErrorLayout(state, context)
            WidgetStateType.HEALTHY,
            WidgetStateType.DUE_SOON,
            WidgetStateType.OVERDUE -> {
                when {
                    size.width < 180.dp -> SmallWidgetContent(state, context)
                    size.height < 140.dp -> MediumWidgetContent(state, context)
                    else -> LargeWidgetContent(state, context)
                }
            }
        }
    }
}

// ─── 1. SMALL WIDGET (Instant Health Check) ──────────────────────────────────
@Composable
private fun SmallWidgetContent(state: AutoMinderWidgetState, context: Context) {
    val isOverdue = state.stateType == WidgetStateType.OVERDUE
    val isDueSoon = state.stateType == WidgetStateType.DUE_SOON

    val statusColor = when {
        isOverdue -> AutoMinderWidget.ColorError
        isDueSoon -> AutoMinderWidget.ColorWarning
        else -> AutoMinderWidget.ColorSuccess
    }

    val iconRes = when {
        isOverdue || isDueSoon -> R.drawable.ic_widget_warning
        else -> R.drawable.ic_widget_check
    }

    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Icon Pill
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(10.dp)
                .background(
                    when {
                        isOverdue -> AutoMinderWidget.ColorErrorContainer
                        isDueSoon -> AutoMinderWidget.ColorWarningContainer
                        else -> AutoMinderWidget.ColorSuccessContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )
        }

        Spacer(GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = state.vehicleName ?: context.getString(R.string.app_name),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            val statusSubtitle = when {
                isOverdue -> "${state.overdueCount} overdue"
                isDueSoon -> state.urgentReminder?.let { "${it.title} soon" } ?: "Due soon"
                else -> context.getString(R.string.widget_all_good_label)
            }

            Text(
                text = statusSubtitle,
                style = TextStyle(
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

// ─── 2. MEDIUM WIDGET (Daily Command Center) ──────────────────────────────────
@Composable
private fun MediumWidgetContent(state: AutoMinderWidgetState, context: Context) {
    val isOverdue = state.stateType == WidgetStateType.OVERDUE
    val isDueSoon = state.stateType == WidgetStateType.DUE_SOON

    val statusColor = when {
        isOverdue -> AutoMinderWidget.ColorError
        isDueSoon -> AutoMinderWidget.ColorWarning
        else -> AutoMinderWidget.ColorSuccess
    }

    Column(modifier = GlanceModifier.fillMaxSize()) {
        // Top Section: Identity + Status + Next Reminder
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon Tile
            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .cornerRadius(12.dp)
                    .background(
                        when {
                            isOverdue -> AutoMinderWidget.ColorErrorContainer
                            isDueSoon -> AutoMinderWidget.ColorWarningContainer
                            else -> AutoMinderWidget.ColorSuccessContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(
                        if (isOverdue || isDueSoon) R.drawable.ic_widget_warning else R.drawable.ic_widget_check
                    ),
                    contentDescription = null,
                    modifier = GlanceModifier.size(22.dp)
                )
            }

            Spacer(GlanceModifier.width(10.dp))

            // Vehicle & Urgent Item Info
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.vehicleName ?: context.getString(R.string.app_name),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )

                    Spacer(GlanceModifier.width(6.dp))

                    if (state.currentOdometerFormatted != null) {
                        Text(
                            text = "· ${state.currentOdometerFormatted}",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(GlanceModifier.height(2.dp))

                val reminderTitle = state.urgentReminder?.title ?: context.getString(R.string.widget_all_caught_up)
                val reminderSubtitle = when {
                    isOverdue -> "Overdue — update or log service"
                    state.urgentReminder?.distanceRemainingKm != null && state.urgentReminder.distanceRemainingKm > 0 ->
                        "~${state.urgentReminder.distanceRemainingKm} ${state.distanceUnit} remaining"
                    state.urgentReminder?.dueDateFormatted != null ->
                        "Due around ${state.urgentReminder.dueDateFormatted}"
                    else -> "All maintenance checks healthy"
                }

                Text(
                    text = reminderTitle,
                    style = TextStyle(
                        color = if (isOverdue) AutoMinderWidget.ColorError else GlanceTheme.colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )

                Text(
                    text = reminderSubtitle,
                    style = TextStyle(
                        color = statusColor,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        // Bottom Action Dock: + Fuel, + Service, + Mileage
        WidgetActionDock(state = state, context = context)
    }
}

// ─── 3. LARGE WIDGET (Mini Dashboard Cockpit) ─────────────────────────────────
@Composable
private fun LargeWidgetContent(state: AutoMinderWidgetState, context: Context) {
    val isOverdue = state.stateType == WidgetStateType.OVERDUE
    val isDueSoon = state.stateType == WidgetStateType.DUE_SOON

    Column(modifier = GlanceModifier.fillMaxSize()) {
        // Header: Vehicle Name + Odometer + Status Pill
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = state.vehicleName ?: context.getString(R.string.app_name),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                if (state.currentOdometerFormatted != null) {
                    Text(
                        text = state.currentOdometerFormatted,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Status Pill
            Box(
                modifier = GlanceModifier
                    .cornerRadius(12.dp)
                    .background(
                        when {
                            isOverdue -> AutoMinderWidget.ColorErrorContainer
                            isDueSoon -> AutoMinderWidget.ColorWarningContainer
                            else -> AutoMinderWidget.ColorSuccessContainer
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(
                            if (isOverdue || isDueSoon) R.drawable.ic_widget_warning else R.drawable.ic_widget_check
                        ),
                        contentDescription = null,
                        modifier = GlanceModifier.size(12.dp)
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = when {
                            isOverdue -> "${state.overdueCount} Overdue"
                            isDueSoon -> "Due Soon"
                            else -> "Healthy"
                        },
                        style = TextStyle(
                            color = when {
                                isOverdue -> AutoMinderWidget.ColorError
                                isDueSoon -> AutoMinderWidget.ColorWarning
                                else -> AutoMinderWidget.ColorSuccess
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        // Middle Bento Card: Urgent Maintenance & Fuel Telemetry
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(12.dp)
                .background(AutoMinderWidget.ColorSurfaceElevated)
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEXT ATTENTION",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    if (state.avgEfficiencyText != null) {
                        Text(
                            text = "Avg ${state.avgEfficiencyText}",
                            style = TextStyle(
                                color = AutoMinderWidget.ColorPrimaryNavy,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(GlanceModifier.height(4.dp))

                val reminderTitle = state.urgentReminder?.title ?: context.getString(R.string.widget_all_caught_up)
                Text(
                    text = reminderTitle,
                    style = TextStyle(
                        color = if (isOverdue) AutoMinderWidget.ColorError else GlanceTheme.colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )

                val forecast = when {
                    isOverdue -> "Past scheduled interval — log completion"
                    state.urgentReminder?.distanceRemainingKm != null && state.urgentReminder.distanceRemainingKm > 0 ->
                        "Scheduled in ~${state.urgentReminder.distanceRemainingKm} ${state.distanceUnit} (${state.urgentReminder.dueDateFormatted ?: ""})"
                    state.urgentReminder?.dueDateFormatted != null ->
                        "Target: ${state.urgentReminder.dueDateFormatted}"
                    else -> "No upcoming maintenance required"
                }

                Text(
                    text = forecast,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        // Bottom Action Dock
        WidgetActionDock(state = state, context = context)
    }
}

// ─── 4. QUICK ACTION DOCK (1-Tap Buttons) ─────────────────────────────────────
@Composable
private fun WidgetActionDock(state: AutoMinderWidgetState, context: Context) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // + Fuel Action Button
        WidgetActionButton(
            title = "+ Fuel",
            iconRes = R.drawable.ic_widget_gas,
            intent = createActionIntent(context, state.vehicleId, MainActivity.ACTION_ADD_FUEL),
            modifier = GlanceModifier.defaultWeight()
        )

        Spacer(GlanceModifier.width(6.dp))

        // + Service Action Button
        WidgetActionButton(
            title = "+ Service",
            iconRes = R.drawable.ic_widget_service,
            intent = createActionIntent(context, state.vehicleId, MainActivity.ACTION_ADD_SERVICE),
            modifier = GlanceModifier.defaultWeight()
        )

        Spacer(GlanceModifier.width(6.dp))

        // + Mileage Action Button
        WidgetActionButton(
            title = "+ Mileage",
            iconRes = R.drawable.ic_widget_mileage,
            intent = createActionIntent(context, state.vehicleId, MainActivity.ACTION_LOG_MILEAGE),
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

@Composable
private fun WidgetActionButton(
    title: String,
    iconRes: Int,
    intent: Intent,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .cornerRadius(10.dp)
            .background(AutoMinderWidget.ColorSurfaceSubtle)
            .clickable(actionStartActivity(intent))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(12.dp)
            )
            Spacer(GlanceModifier.width(4.dp))
            Text(
                text = title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}

// ─── 5. DEDICATED EMPTY / SKELETON / ERROR STATES ─────────────────────────────
@Composable
private fun WidgetEmptyLayout(context: Context) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(10.dp)
                .background(AutoMinderWidget.ColorSurfaceSubtle),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_service),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp)
            )
        }

        Spacer(GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = context.getString(R.string.dashboard_empty_title),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Tap to add your first car",
                style = TextStyle(
                    color = AutoMinderWidget.ColorPrimaryNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun WidgetSetupIncompleteLayout(state: AutoMinderWidgetState, context: Context) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(10.dp)
                .background(AutoMinderWidget.ColorWarningContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_warning),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp)
            )
        }

        Spacer(GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = state.vehicleName ?: "Vehicle Setup",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Tap to record current mileage",
                style = TextStyle(
                    color = AutoMinderWidget.ColorWarning,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun WidgetErrorLayout(state: AutoMinderWidgetState, context: Context) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(10.dp)
                .background(AutoMinderWidget.ColorErrorContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_warning),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp)
            )
        }

        Spacer(GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "Milevora",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = state.errorMessage ?: "Tap to refresh vehicle state",
                style = TextStyle(
                    color = AutoMinderWidget.ColorError,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun WidgetLoadingLayout() {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(10.dp)
                .background(AutoMinderWidget.ColorSurfaceSubtle)
        ) {}
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Box(
                modifier = GlanceModifier
                    .height(14.dp)
                    .fillMaxWidth()
                    .cornerRadius(4.dp)
                    .background(AutoMinderWidget.ColorSurfaceSubtle)
            ) {}
            Spacer(GlanceModifier.height(6.dp))
            Box(
                modifier = GlanceModifier
                    .height(10.dp)
                    .width(100.dp)
                    .cornerRadius(4.dp)
                    .background(AutoMinderWidget.ColorSurfaceSubtle)
            ) {}
        }
    }
}

// ─── INTENT BUILDERS ──────────────────────────────────────────────────────────
private fun createMainIntent(context: Context, vehicleId: Long?): Intent {
    return Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (vehicleId != null && vehicleId > 0) {
            putExtra(NotificationHelper.EXTRA_VEHICLE_ID, vehicleId)
        }
    }
}

private fun createActionIntent(context: Context, vehicleId: Long?, action: String): Intent {
    return Intent(context, MainActivity::class.java).apply {
        this.action = Intent.ACTION_VIEW
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(MainActivity.EXTRA_WIDGET_ACTION, action)
        if (vehicleId != null && vehicleId > 0) {
            putExtra(NotificationHelper.EXTRA_VEHICLE_ID, vehicleId)
        }
    }
}

class AutoMinderWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AutoMinderWidget()
}
