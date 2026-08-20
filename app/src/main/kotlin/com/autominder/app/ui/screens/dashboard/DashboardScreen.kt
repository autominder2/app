package com.autominder.app.ui.screens.dashboard

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.core.util.PowerSettings
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.usecase.ReminderWithStatus
import com.autominder.app.domain.usecase.VehicleWithStatus
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.components.DashboardSkeleton
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.RemindersDelayedBanner
import com.autominder.app.ui.components.StatusChip
import com.autominder.app.ui.components.premium.MaintenanceVerdictCard
import com.autominder.app.ui.components.premium.PremiumSectionHeader
import com.autominder.app.ui.components.premium.VehicleHeroCard
import com.autominder.app.ui.components.premium.VehicleHeroVariant
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.theme.Motion
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.OverdueCopy
import com.autominder.app.ui.util.localizedLabel
import com.autominder.app.ui.util.overdueByText
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private enum class QuickAction { LOG_SERVICE, ADD_FUEL, AUDIT_QUOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToVehicleDetail: (Long) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToAddService: (Long) -> Unit,
    onNavigateToAddFuel: (Long) -> Unit,
    onNavigateToQuoteAuditor: (Long?) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remindersDelayed by viewModel.remindersDelayed.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val fabExtended by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Trigger In-App Review check when Dashboard appears
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        if (activity != null) {
            viewModel.requestReviewIfAppropriate(activity)
        }
    }

    val vehicles = (uiState as? DashboardUiState.Success)?.vehicles ?: emptyList()
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<QuickAction?>(null) }

    // Back gesture closes the quick-actions menu instead of leaving the screen.
    BackHandler(enabled = fabMenuExpanded) { fabMenuExpanded = false }

    fun launchQuickAction(action: QuickAction) {
        fabMenuExpanded = false
        when {
            action == QuickAction.AUDIT_QUOTE -> {
                val id = vehicles.firstOrNull()?.vehicle?.id
                onNavigateToQuoteAuditor(id)
            }
            vehicles.isEmpty() -> onNavigateToAddVehicle()
            vehicles.size == 1 -> {
                val id = vehicles.first().vehicle.id
                if (action == QuickAction.LOG_SERVICE) onNavigateToAddService(id) else onNavigateToAddFuel(id)
            }
            else -> pendingAction = action
        }
    }

    if (pendingAction != null) {
        val pickerSheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { pendingAction = null },
            sheetState = pickerSheetState
        ) {
            Text(
                text = stringResource(R.string.dashboard_pick_vehicle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            vehicles.forEach { item ->
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.vehicle_make_model, item.vehicle.make, item.vehicle.model))
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        val action = pendingAction
                        pendingAction = null
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        when (action) {
                            QuickAction.LOG_SERVICE -> onNavigateToAddService(item.vehicle.id)
                            QuickAction.ADD_FUEL -> onNavigateToAddFuel(item.vehicle.id)
                            QuickAction.AUDIT_QUOTE -> onNavigateToQuoteAuditor(item.vehicle.id)
                            null -> Unit
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DashboardTopBar(scrollBehavior)
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = fabMenuExpanded,
                    enter = fadeIn() + slideInVertically { it / 3 },
                    exit = fadeOut() + slideOutVertically { it / 3 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        QuickActionRow(
                            label = stringResource(R.string.quick_action_audit_quote),
                            icon = Icons.Default.Build,
                            onClick = { launchQuickAction(QuickAction.AUDIT_QUOTE) }
                        )
                        QuickActionRow(
                            label = stringResource(R.string.dashboard_quick_log_service),
                            icon = Icons.Default.Build,
                            onClick = { launchQuickAction(QuickAction.LOG_SERVICE) }
                        )
                        QuickActionRow(
                            label = stringResource(R.string.dashboard_quick_add_fuel),
                            icon = Icons.Default.LocalGasStation,
                            onClick = { launchQuickAction(QuickAction.ADD_FUEL) }
                        )
                        QuickActionRow(
                            label = stringResource(R.string.action_add_vehicle),
                            icon = Icons.Default.DirectionsCar,
                            onClick = {
                                fabMenuExpanded = false
                                onNavigateToAddVehicle()
                            }
                        )
                    }
                }

                val fabRotation by animateFloatAsState(
                    targetValue = if (fabMenuExpanded) 45f else 0f,
                    animationSpec = Motion.springSnappy(),
                    label = "fab_rotation"
                )
                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (vehicles.isEmpty()) {
                            onNavigateToAddVehicle()
                        } else {
                            fabMenuExpanded = !fabMenuExpanded
                        }
                    },
                    expanded = fabExtended && !fabMenuExpanded,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_quick_actions),
                            modifier = Modifier.rotate(fabRotation)
                        )
                    },
                    text = { Text(stringResource(R.string.dashboard_quick_actions)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is DashboardUiState.Loading -> DashboardSkeleton()
                    is DashboardUiState.Empty -> EmptyState(
                        title = stringResource(R.string.dashboard_no_vehicles_title),
                        subtitle = stringResource(R.string.dashboard_no_vehicles_subtitle),
                        onAction = onNavigateToAddVehicle,
                        actionLabel = stringResource(R.string.action_add_vehicle),
                        icon = Icons.Default.Commute,
                        hint = stringResource(R.string.dashboard_add_first_vehicle_hint)
                    )
                    is DashboardUiState.Error -> ErrorState(
                        message = stringResource(state.messageRes ?: R.string.dashboard_error),
                        onRetry = { viewModel.retry() }
                    )
                    is DashboardUiState.Success -> DashboardBentoContent(
                        vehicles = state.vehicles,
                        attentionReminders = state.attentionReminders,
                        primaryCostPerDistanceCents = state.primaryCostPerDistanceCents,
                        primaryAvgEfficiency = state.primaryAvgEfficiency,
                        remindersDelayed = remindersDelayed,
                        onFixDelayedClick = { PowerSettings.openBatteryOptimizationSettings(context) },
                        onVehicleClick = onNavigateToVehicleDetail,
                        onLogServiceClick = { launchQuickAction(QuickAction.LOG_SERVICE) },
                        onAddFuelClick = { launchQuickAction(QuickAction.ADD_FUEL) },
                        onAddVehicleClick = onNavigateToAddVehicle,
                        listState = listState
                    )
                }
            }

            // Scrim under the open quick-actions menu — tap anywhere to dismiss.
            AnimatedVisibility(
                visible = fabMenuExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { fabMenuExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val handleAction = {
        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
        onClick()
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = handleAction)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp,
            onClick = handleAction
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        SmallFloatingActionButton(
            onClick = handleAction,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> stringResource(R.string.dashboard_greeting_morning)
        in 12..16 -> stringResource(R.string.dashboard_greeting_afternoon)
        else -> stringResource(R.string.dashboard_greeting_evening)
    }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_top_bar_title),
                    fontFamily = Exo2,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * 2026 Flagship Bento Grid Cockpit Content
 */
@Composable
private fun DashboardBentoContent(
    vehicles: List<VehicleWithStatus>,
    attentionReminders: List<ReminderWithStatus>,
    primaryCostPerDistanceCents: Double?,
    primaryAvgEfficiency: Double?,
    remindersDelayed: RemindersDelayedState?,
    onFixDelayedClick: () -> Unit,
    onVehicleClick: (Long) -> Unit,
    onLogServiceClick: () -> Unit,
    onAddFuelClick: () -> Unit,
    onAddVehicleClick: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val distanceUnit = LocalDistanceUnit.current
    val totalOverdue = remember(vehicles) { vehicles.sumOf { it.overdueCount } }
    val totalDueSoon = remember(vehicles) { vehicles.sumOf { it.dueSoonCount } }
    val attentionTotal = totalOverdue + totalDueSoon
    val worstStatus = when {
        totalOverdue > 0 -> ServiceStatus.OVERDUE
        totalDueSoon > 0 -> ServiceStatus.DUE_SOON
        else -> ServiceStatus.OK
    }

    val primaryVehicle = vehicles.firstOrNull()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Maintenance Verdict Banner
        item(key = "cockpit_verdict") {
            MaintenanceVerdictCard(
                headlineText = if (attentionTotal > 0) {
                    pluralStringResource(
                        R.plurals.dashboard_attention_headline, attentionTotal, attentionTotal
                    )
                } else {
                    stringResource(R.string.dashboard_all_clear_headline)
                },
                supportingText = if (attentionTotal > 0) {
                    stringResource(R.string.dashboard_cockpit_supporting)
                } else {
                    stringResource(R.string.dashboard_all_clear_supporting)
                },
                status = worstStatus,
                modifier = Modifier.animateItem()
            )
        }

        // 2. Delayed Reminder Alert if engine is silent
        if (remindersDelayed != null) {
            item(key = "reminders_delayed") {
                RemindersDelayedBanner(
                    lastCheckedAt = remindersDelayed.lastCheckedAt,
                    onFixClick = onFixDelayedClick,
                    modifier = Modifier.animateItem()
                )
            }
        }

        // 3. Flagship Hero Digital Twin (Primary Vehicle)
        if (primaryVehicle != null) {
            item(key = "hero_vehicle") {
                val vehicle = primaryVehicle.vehicle
                val title = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model)
                val formattedOdometer = remember(vehicle.currentOdometer, distanceUnit) {
                    DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit)) +
                        " " + DistanceUtil.unitLabel(distanceUnit)
                }
                val statusLabel = statusLabelFor(primaryVehicle.status)

                VehicleHeroCard(
                    title = title,
                    variant = VehicleHeroVariant.Expanded,
                    yearText = vehicle.year.takeIf { it > 0 }?.toString(),
                    odometerText = formattedOdometer,
                    photoUri = vehicle.photoUri,
                    mergedContentDescription = "$title, $formattedOdometer, $statusLabel",
                    statusChip = { StatusChip(status = primaryVehicle.status) },
                    railStatus = primaryVehicle.status,
                    onClick = { onVehicleClick(vehicle.id) },
                    modifier = Modifier.animateItem()
                )
            }

            // 4. Precision Telemetry Bento Grid
            item(key = "telemetry_bento_grid") {
                val vehicle = primaryVehicle.vehicle
                val formattedOdometer = remember(vehicle.currentOdometer, distanceUnit) {
                    DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit))
                }
                val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Odometer & Attention Alerts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Odometer Bento Pill
                        TelemetryBentoPill(
                            title = stringResource(R.string.vehicle_detail_odometer).uppercase(),
                            value = "$formattedOdometer ${DistanceUtil.unitLabel(distanceUnit)}",
                            subtitle = stringResource(R.string.vehicle_detail_odometer),
                            icon = Icons.Default.Speed,
                            onClick = { onVehicleClick(vehicle.id) },
                            modifier = Modifier.weight(1f)
                        )

                        // Status / Alerts Bento Pill
                        TelemetryBentoPill(
                            title = stringResource(R.string.dashboard_section_attention).uppercase(),
                            value = if (attentionTotal > 0) "$attentionTotal Due" else stringResource(R.string.dashboard_all_clear_headline),
                            subtitle = if (attentionTotal > 0) stringResource(R.string.vehicle_detail_needs_attention) else stringResource(R.string.dashboard_all_clear_supporting),
                            icon = Icons.Default.Build,
                            isAlert = attentionTotal > 0,
                            onClick = { onVehicleClick(vehicle.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Live Fuel & Cost Telemetry (when fuel records exist)
                    if (primaryCostPerDistanceCents != null || primaryAvgEfficiency != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (primaryCostPerDistanceCents != null) {
                                TelemetryBentoPill(
                                    title = stringResource(R.string.fuel_cost_per_distance_label).uppercase(),
                                    value = currencyFormat.format(primaryCostPerDistanceCents / 100.0) + " / " + DistanceUtil.unitLabel(distanceUnit),
                                    subtitle = stringResource(R.string.dashboard_daily_telemetry_title),
                                    icon = Icons.Default.Route,
                                    onClick = onAddFuelClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (primaryAvgEfficiency != null) {
                                TelemetryBentoPill(
                                    title = stringResource(R.string.fuel_avg_economy_label).uppercase(),
                                    value = String.format(Locale.getDefault(), "%.1f %s", primaryAvgEfficiency, if (distanceUnit == "mi") "MPG" else "km/L"),
                                    subtitle = stringResource(R.string.fuel_intelligence_title),
                                    icon = Icons.Default.LocalGasStation,
                                    onClick = onAddFuelClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Quick Action Capsule Dock
            item(key = "quick_action_dock") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionDockCapsule(
                        label = stringResource(R.string.dashboard_quick_log_service),
                        icon = Icons.Default.Build,
                        onClick = onLogServiceClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionDockCapsule(
                        label = stringResource(R.string.dashboard_quick_add_fuel),
                        icon = Icons.Default.LocalGasStation,
                        onClick = onAddFuelClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionDockCapsule(
                        label = stringResource(R.string.action_add_vehicle),
                        icon = Icons.Default.DirectionsCar,
                        onClick = onAddVehicleClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. Priority Triage Stream
        if (attentionReminders.isNotEmpty()) {
            item(key = "attention_header") {
                PremiumSectionHeader(
                    title = stringResource(R.string.dashboard_section_attention),
                    modifier = Modifier.animateItem()
                )
            }
            items(attentionReminders, key = { "attention_${it.reminder.id}" }) { entry ->
                val vehicle = entry.vehicle
                AttentionStatusRow(
                    title = entry.reminder.customLabel
                        ?: entry.reminder.serviceType.localizedLabel(),
                    detail = attentionReason(entry, distanceUnit),
                    status = entry.status,
                    onClick = vehicle?.let { v -> { onVehicleClick(v.id) } },
                    modifier = Modifier.animateItem()
                )
            }
        }

        // 7. Additional Garage Vehicles (if more than 1)
        if (vehicles.size > 1) {
            item(key = "other_vehicles_header") {
                PremiumSectionHeader(
                    title = stringResource(R.string.dashboard_section_vehicles),
                    countText = vehicles.size.toString(),
                    modifier = Modifier.animateItem()
                )
            }
            items(vehicles.drop(1), key = { it.vehicle.id }) { vehicleWithStatus ->
                val vehicle = vehicleWithStatus.vehicle
                val title = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model)
                val formattedOdometer = remember(vehicle.currentOdometer, distanceUnit) {
                    DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit)) +
                        " " + DistanceUtil.unitLabel(distanceUnit)
                }
                val statusLabel = statusLabelFor(vehicleWithStatus.status)

                VehicleHeroCard(
                    title = title,
                    variant = VehicleHeroVariant.Compact,
                    yearText = vehicle.year.takeIf { it > 0 }?.toString(),
                    odometerText = formattedOdometer,
                    photoUri = vehicle.photoUri,
                    mergedContentDescription = "$title, $formattedOdometer, $statusLabel",
                    statusChip = { StatusChip(status = vehicleWithStatus.status) },
                    railStatus = vehicleWithStatus.status,
                    onClick = { onVehicleClick(vehicle.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        // Bottom clearance for floating controls
        item(key = "fab_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * 2026 Telemetry Bento Pill (Precision Metric Box)
 */
@Composable
private fun TelemetryBentoPill(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onClick()
        },
        interactionSource = interactionSource,
        modifier = modifier
            .pressScale(interactionSource),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 2026 Quick Action Capsule Dock Pill
 */
@Composable
private fun QuickActionDockCapsule(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onClick()
        },
        interactionSource = interactionSource,
        modifier = modifier
            .pressScale(interactionSource)
            .height(48.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/**
 * One attention item, as a compact row with leading vertical accent rail.
 */
@Composable
private fun AttentionStatusRow(
    title: String,
    detail: String,
    status: ServiceStatus,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val beacon = when (status) {
        ServiceStatus.OVERDUE -> MaterialTheme.colorScheme.error
        ServiceStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiary
        ServiceStatus.SNOOZED, ServiceStatus.UNKNOWN -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.primary
    }
    val statusLabel = statusLabelFor(status)
    val interactionSource = remember { MutableInteractionSource() }
    val rowModifier = modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        .then(
            if (onClick != null) {
                Modifier
                    .pressScale(interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = androidx.compose.foundation.LocalIndication.current,
                        onClick = onClick
                    )
            } else Modifier
        )
        .heightIn(min = 64.dp)
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .semantics(mergeDescendants = true) {
            contentDescription = "$title, $statusLabel, $detail"
        }

    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(beacon)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun statusLabelFor(status: ServiceStatus): String = stringResource(
    when (status) {
        ServiceStatus.OVERDUE -> R.string.status_overdue
        ServiceStatus.DUE_SOON -> R.string.status_due_soon
        ServiceStatus.SNOOZED -> R.string.status_snoozed
        ServiceStatus.OK -> R.string.status_ok
        ServiceStatus.COMPLETED -> R.string.status_completed
        ServiceStatus.UNKNOWN -> R.string.status_unknown
    }
)

@Composable
private fun attentionReason(
    entry: ReminderWithStatus,
    distanceUnit: String
): String {
    val reminder = entry.reminder
    val currentOdometer = entry.vehicle?.currentOdometer
    val dueOdometer = reminder.nextDueOdometer

    val overdueByMileage = entry.status == ServiceStatus.OVERDUE &&
        dueOdometer != null && currentOdometer != null && currentOdometer >= dueOdometer

    val exactDistanceIsUseful = overdueByMileage && OverdueCopy.showsExactDistance(
        overdueKm = currentOdometer!! - dueOdometer!!,
        intervalKm = reminder.intervalKm
    )

    return when {
        exactDistanceIsUseful -> overdueByText(
            overdueKm = currentOdometer - dueOdometer,
            intervalKm = reminder.intervalKm,
            distanceUnit = distanceUnit
        )
        overdueByMileage -> {
            val wasDueAt = remember(dueOdometer, distanceUnit) {
                DistanceFormat.grouped(DistanceUtil.kmToDisplay(dueOdometer, distanceUnit))
            }
            stringResource(
                R.string.reminder_was_due_at, wasDueAt, DistanceUtil.unitLabel(distanceUnit)
            )
        }
        reminder.nextDueDate != null -> stringResource(
            R.string.vehicle_detail_due_date, DateFormatUtil.formatDate(reminder.nextDueDate)
        )
        dueOdometer != null -> {
            val dueAt = remember(dueOdometer, distanceUnit) {
                DistanceFormat.grouped(DistanceUtil.kmToDisplay(dueOdometer, distanceUnit))
            }
            stringResource(
                R.string.vehicle_detail_due_at_dynamic, dueAt, DistanceUtil.unitLabel(distanceUnit)
            )
        }
        else -> statusLabelFor(entry.status)
    }
}
