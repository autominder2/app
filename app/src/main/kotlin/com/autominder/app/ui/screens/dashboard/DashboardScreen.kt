package com.autominder.app.ui.screens.dashboard

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import com.autominder.app.ui.theme.Motion
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import java.util.Calendar
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.hilt.navigation.compose.hiltViewModel
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.usecase.VehicleWithStatus
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.FleetHealthScore
import com.autominder.app.ui.components.LoadingState
import com.autominder.app.ui.components.DashboardSkeleton
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.components.StatusChip
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.LocalDistanceUnit
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.localizedLabel
import com.autominder.app.domain.usecase.ReminderWithStatus
import com.autominder.app.ui.components.premium.HealthCockpitCard
import com.autominder.app.ui.components.premium.InsightMetricCard
import com.autominder.app.ui.components.premium.InsightMetricRow
import com.autominder.app.ui.components.premium.PremiumSectionHeader
import com.autominder.app.ui.components.premium.ProactiveAttentionCard
import com.autominder.app.ui.components.premium.VehicleHeroCard
import androidx.compose.ui.res.pluralStringResource

private enum class QuickAction { LOG_SERVICE, ADD_FUEL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToVehicleDetail: (Long) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToAddService: (Long) -> Unit,
    onNavigateToAddFuel: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                        if (action == QuickAction.LOG_SERVICE) {
                            onNavigateToAddService(item.vehicle.id)
                        } else {
                            onNavigateToAddFuel(item.vehicle.id)
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
                    is DashboardUiState.Success -> DashboardContent(
                        vehicles = state.vehicles,
                        attentionReminders = state.attentionReminders,
                        onVehicleClick = onNavigateToVehicleDetail,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        SmallFloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            },
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

@Composable
private fun DashboardContent(
    vehicles: List<VehicleWithStatus>,
    attentionReminders: List<ReminderWithStatus>,
    onVehicleClick: (Long) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val distanceUnit = LocalDistanceUnit.current
    val totalOverdue = remember(vehicles) { vehicles.sumOf { it.overdueCount } }
    val totalDueSoon = remember(vehicles) { vehicles.sumOf { it.dueSoonCount } }
    val attentionTotal = totalOverdue + totalDueSoon
    // Same math the old fleet ring used — kept as the secondary instrument.
    val fleetScore = remember(vehicles) {
        (100 - totalOverdue * 25 - totalDueSoon * 10).coerceIn(0, 100)
    }
    val worstStatus = when {
        totalOverdue > 0 -> ServiceStatus.OVERDUE
        totalDueSoon > 0 -> ServiceStatus.DUE_SOON
        else -> ServiceStatus.OK
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // The cockpit verdict: a sentence, not a naked number.
        item(key = "cockpit") {
            HealthCockpitCard(
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
                score = fleetScore,
                scoreDescription = stringResource(R.string.cd_health_score, fleetScore),
                modifier = Modifier.animateItem()
            )
        }

        if (attentionTotal > 0) {
            item(key = "metrics") {
                InsightMetricRow(modifier = Modifier.animateItem()) {
                    InsightMetricCard(
                        label = stringResource(R.string.dashboard_metric_overdue),
                        value = totalOverdue.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    InsightMetricCard(
                        label = stringResource(R.string.dashboard_metric_due_soon),
                        value = totalDueSoon.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (attentionReminders.isNotEmpty()) {
            item(key = "attention_header") {
                PremiumSectionHeader(
                    title = stringResource(R.string.dashboard_section_attention),
                    countText = attentionTotal.toString(),
                    modifier = Modifier.animateItem()
                )
            }
            items(attentionReminders, key = { "attention_${it.reminder.id}" }) { entry ->
                val vehicle = entry.vehicle
                ProactiveAttentionCard(
                    title = entry.reminder.customLabel
                        ?: entry.reminder.serviceType.localizedLabel(),
                    reasonText = attentionReason(entry, distanceUnit),
                    status = entry.status,
                    ctaLabel = if (vehicle != null) {
                        stringResource(R.string.dashboard_attention_cta)
                    } else {
                        null
                    },
                    onCta = vehicle?.let { v -> { onVehicleClick(v.id) } },
                    onClick = vehicle?.let { v -> { onVehicleClick(v.id) } },
                    modifier = Modifier.animateItem()
                )
            }
        }

        item(key = "vehicles_header") {
            PremiumSectionHeader(
                title = stringResource(R.string.dashboard_section_vehicles),
                countText = vehicles.size.toString(),
                modifier = Modifier.animateItem()
            )
        }
        items(vehicles, key = { it.vehicle.id }) { vehicleWithStatus ->
            val vehicle = vehicleWithStatus.vehicle
            val title = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model)
            val formattedOdometer = remember(vehicle.currentOdometer, distanceUnit) {
                DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit)) +
                    " " + DistanceUtil.unitLabel(distanceUnit)
            }
            val statusLabel = statusLabelFor(vehicleWithStatus.status)
            val overdueSuffix = if (vehicleWithStatus.overdueCount > 0) {
                ", " + stringResource(R.string.dashboard_overdue_count, vehicleWithStatus.overdueCount)
            } else {
                ""
            } + if (vehicleWithStatus.dueSoonCount > 0) {
                ", " + stringResource(R.string.dashboard_due_soon_count, vehicleWithStatus.dueSoonCount)
            } else {
                ""
            }
            VehicleHeroCard(
                title = title,
                yearText = vehicle.year.takeIf { it > 0 }?.toString(),
                odometerText = formattedOdometer,
                photoUri = vehicle.photoUri,
                mergedContentDescription = "$title, $formattedOdometer, $statusLabel$overdueSuffix",
                statusChip = { StatusChip(status = vehicleWithStatus.status) },
                onClick = { onVehicleClick(vehicle.id) },
                modifier = Modifier.animateItem()
            )
        }

        // Clearance so the FAB never covers the last card's actions.
        item(key = "fab_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/** Localized display label for a status — used in merged TalkBack summaries. */
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

/**
 * One-line human reason for an attention card — same truth contract as the
 * reminder cards: a mileage-fired overdue leads with the mileage story,
 * never a future-looking date.
 */
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
    return when {
        overdueByMileage -> {
            val overBy = remember(currentOdometer, dueOdometer, distanceUnit) {
                DistanceFormat.grouped(
                    DistanceUtil.kmToDisplay(currentOdometer!! - dueOdometer!!, distanceUnit)
                )
            }
            stringResource(
                R.string.vehicle_detail_overdue_by_km, overBy, DistanceUtil.unitLabel(distanceUnit)
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
