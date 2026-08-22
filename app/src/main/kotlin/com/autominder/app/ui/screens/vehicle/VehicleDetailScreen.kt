package com.autominder.app.ui.screens.vehicle

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.theme.Exo2
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.usecase.DuePrediction
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.LocalIsProUser
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.ListSkeleton
import com.autominder.app.domain.util.VehicleDisplayNameFormatter
import com.autominder.app.ui.components.AutoMinderServiceStatusBadge
import com.autominder.app.ui.components.ProFeatureGate
import com.autominder.app.ui.components.QuickMileageSheet
import com.autominder.app.ui.components.ReminderDetailSheet
import com.autominder.app.ui.components.StatusChip
import com.autominder.app.ui.components.charts.CostByTypeDonut
import com.autominder.app.ui.components.charts.FuelEfficiencyChart
import com.autominder.app.ui.components.charts.SpendingTrendChart
import com.autominder.app.ui.util.DateFormatUtil
import androidx.compose.material3.SnackbarDuration
import com.autominder.app.ui.components.LocalSnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel
import com.autominder.app.ui.util.overdueByText
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.res.pluralStringResource
import com.autominder.app.ui.components.premium.MaintenanceVerdictCard
import com.autominder.app.ui.components.premium.PremiumAction
import com.autominder.app.ui.components.premium.PremiumActionGrid
import com.autominder.app.ui.components.premium.PremiumSectionHeader
import com.autominder.app.ui.components.premium.StatusReminderCard
import com.autominder.app.ui.components.premium.VehicleHeroCard
import com.autominder.app.ui.components.premium.VehicleHeroVariant
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    onNavigateBack: () -> Unit,
    /** True when the notification "Update mileage" action opened this screen. */
    openMileageSheet: Boolean = false,
    mileageRequestId: Long = 0L,
    onNavigateToAddReminder: (Long) -> Unit,
    onNavigateToEditVehicle: (Long) -> Unit = {},
    onNavigateToAddService: (Long) -> Unit = {},
    onNavigateToMileageLog: (Long) -> Unit = {},
    onNavigateToEditReminder: (Long) -> Unit = {},
    onNavigateToAddFuel: (Long) -> Unit = {},
    onNavigateToFuelHistory: (Long) -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isProUser = LocalIsProUser.current
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isArchived) {
        if (uiState.isArchived) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.exportUri) {
        val uri = uiState.exportUri ?: return@LaunchedEffect
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.vehicle_detail_export_share_title))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.vehicle_detail_export)))
        viewModel.onEvent(VehicleDetailUiEvent.ExportConsumed)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    // The hero already says the vehicle's name — the app bar stays quiet until
    // the hero scrolls away, then takes over. Kills the duplicate-title read.
    val showBarTitle by remember {
        androidx.compose.runtime.derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 220
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    if (showBarTitle) {
                        Text(
                            text = uiState.vehicle?.let {
                                stringResource(R.string.vehicle_make_model, it.make, it.model)
                            } ?: stringResource(R.string.vehicle_detail_title),
                            fontFamily = Exo2,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    val vehicle = uiState.vehicle
                    if (vehicle != null) {
                        IconButton(onClick = { onNavigateToEditVehicle(vehicle.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        var showArchiveDialog by remember { mutableStateOf(false) }
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options)
                            )
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.vehicle_detail_export)) },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.onEvent(VehicleDetailUiEvent.ExportClicked)
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_archive)) },
                                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showArchiveDialog = true
                                }
                            )
                        }

                        if (showArchiveDialog) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showArchiveDialog = false },
                                title = { Text(stringResource(R.string.action_archive)) },
                                text = { Text(stringResource(R.string.vehicle_detail_archive_message)) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showArchiveDialog = false
                                        viewModel.onEvent(VehicleDetailUiEvent.ArchiveClicked)
                                    }) {
                                        Text(stringResource(R.string.action_archive), color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showArchiveDialog = false }) {
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val vehicle = uiState.vehicle
            if (vehicle != null) {
                FloatingActionButton(
                    onClick = { onNavigateToAddReminder(vehicle.id) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_reminder))
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = ScreenState.fromUiState(uiState),
            label = "ScreenState",
            modifier = Modifier.padding(padding)
        ) { state ->
            when (state) {
                ScreenState.Loading -> ListSkeleton(rows = 5)
                ScreenState.Empty -> EmptyState(
                    title = stringResource(R.string.vehicle_detail_not_found),
                    subtitle = stringResource(R.string.vehicle_detail_not_found_subtitle)
                )
                ScreenState.Error -> ErrorState(
                    message = uiState.errorRes?.let { stringResource(it, *uiState.errorArgs.toTypedArray()) }
                        ?: stringResource(R.string.vehicle_detail_unknown_error),
                    onRetry = { viewModel.retry() }
                )
                ScreenState.Success -> uiState.vehicle?.let { v ->
                    VehicleDetailContent(
                        vehicle = v,
                        autoOpenMileageSheet = openMileageSheet,
                        mileageRequestId = mileageRequestId,
                        listState = listState,
                        onAddReminder = { onNavigateToAddReminder(v.id) },
                        reminders = uiState.reminders,
                        reminderStatuses = uiState.reminderStatuses,
                        reminderPredictions = uiState.reminderPredictions,
                        totalCostCents = uiState.totalCostCents,
                        yearCostCents = uiState.yearCostCents,
                        averageEfficiency = uiState.averageEfficiency,
                        monthlySpending = uiState.monthlySpending,
                        costByType = uiState.costByType,
                        efficiencySeries = uiState.efficiencySeries,
                        costPerKmCents = uiState.costPerKmCents,
                        isProUser = isProUser,
                        onAddServiceClick = { onNavigateToAddService(v.id) },
                        onMileageLogClick = { onNavigateToMileageLog(v.id) },
                        onAddFuelClick = { onNavigateToAddFuel(v.id) },
                        onFuelHistoryClick = { onNavigateToFuelHistory(v.id) },
                        onNavigateToPaywall = onNavigateToPaywall,
                        onMarkComplete = { reminderId ->
                            viewModel.onEvent(VehicleDetailUiEvent.MarkReminderComplete(reminderId))
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.reminder_marked_complete),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onSnooze = { reminderId ->
                            viewModel.onEvent(VehicleDetailUiEvent.SnoozeReminder(reminderId))
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.reminder_snoozed),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onEditReminder = { reminderId ->
                            onNavigateToEditReminder(reminderId)
                        },
                        onUpdateOdometer = { odometerKm ->
                            viewModel.onEvent(VehicleDetailUiEvent.UpdateOdometer(odometerKm))
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.odometer_updated),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private enum class ScreenState {
    Loading, Empty, Error, Success;

    companion object {
        fun fromUiState(uiState: VehicleDetailUiState): ScreenState {
            return when {
                uiState.isLoading -> Loading
                uiState.errorRes != null -> Error
                uiState.vehicle == null -> Empty
                else -> Success
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleDetailContent(
    vehicle: Vehicle,
    autoOpenMileageSheet: Boolean = false,
    mileageRequestId: Long = 0L,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onAddReminder: () -> Unit,
    reminders: List<Reminder>,
    reminderStatuses: Map<Long, ServiceStatus>,
    reminderPredictions: Map<Long, DuePrediction>,
    totalCostCents: Int,
    yearCostCents: Int,
    averageEfficiency: Double,
    monthlySpending: List<MonthlySpend>,
    costByType: List<TypeSpend>,
    efficiencySeries: List<Double>,
    costPerKmCents: Double?,
    isProUser: Boolean,
    onAddServiceClick: () -> Unit,
    onMileageLogClick: () -> Unit,
    onAddFuelClick: () -> Unit,
    onFuelHistoryClick: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onMarkComplete: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onEditReminder: (Long) -> Unit,
    onUpdateOdometer: (Int) -> Unit
) {
    var showMileageSheet by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(false)
    }
    var consumedMileageRequestId by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableLongStateOf(Long.MIN_VALUE)
    }
    androidx.compose.runtime.LaunchedEffect(autoOpenMileageSheet, mileageRequestId) {
        if (autoOpenMileageSheet && consumedMileageRequestId != mileageRequestId) {
            consumedMileageRequestId = mileageRequestId
            showMileageSheet = true
        }
    }
    var showAllAttention by remember { mutableStateOf(false) }
    val mileageSheetState = rememberModalBottomSheetState()
    val distanceUnit = LocalDistanceUnit.current
    val haptic = LocalHapticFeedback.current
    val onUpgradeClick: () -> Unit = onNavigateToPaywall

    // Consumer-grade issue detail (the FIXD pattern): tap a reminder to get
    // plain language, "can it wait?", and the personalized forecast.
    var detailReminder by remember { mutableStateOf<Reminder?>(null) }
    val onReminderClick: (Reminder) -> Unit = { detailReminder = it }
    detailReminder?.let { selected ->
        ReminderDetailSheet(
            reminder = selected,
            status = reminderStatuses[selected.id] ?: ServiceStatus.UNKNOWN,
            prediction = reminderPredictions[selected.id],
            currentOdometerKm = vehicle.currentOdometer,
            onMarkComplete = {
                detailReminder = null
                onMarkComplete(selected.id)
            },
            onSnooze = {
                detailReminder = null
                onSnooze(selected.id)
            },
            onEdit = {
                detailReminder = null
                onEditReminder(selected.id)
            },
            onDismiss = { detailReminder = null }
        )
    }

    if (showMileageSheet) {
        QuickMileageSheet(
            currentOdometer = DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit),
            unitLabel = DistanceUtil.unitLabel(distanceUnit),
            sheetState = mileageSheetState,
            onDismiss = { showMileageSheet = false },
            onUpdate = { newDisplayOdometer ->
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onUpdateOdometer(DistanceUtil.displayToKm(newDisplayOdometer, distanceUnit))
                showMileageSheet = false
            }
        )
    }

    // Worst active status drives the hero chip and the diagnosis card.
    val worstStatus = remember(reminders, reminderStatuses) {
        reminders
            .mapNotNull { reminderStatuses[it.id] }
            .maxByOrNull { it.severity }
    }
    // Triage lists — computed once per data change, in composable scope
    // (LazyListScope builders can't call remember).
    val needsAttention = remember(reminders, reminderStatuses) {
        reminders
            .filter {
                (reminderStatuses[it.id] ?: ServiceStatus.UNKNOWN) in
                    setOf(ServiceStatus.OVERDUE, ServiceStatus.DUE_SOON)
            }
            .sortedByDescending { reminderStatuses[it.id]?.severity ?: 0 }
    }
    val upcoming = remember(reminders, needsAttention) {
        reminders.filterNot { it in needsAttention }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1 — The car is the hero. Name appears exactly once on screen.
        item(key = "hero") {
            VehicleHeroCard(
                title = VehicleDisplayNameFormatter.format(vehicle.make, vehicle.model, vehicle.year),
                variant = VehicleHeroVariant.Expanded,
                yearText = vehicle.year.takeIf { it > 0 }?.toString(),
                photoUri = vehicle.photoUri,
                photoContentDescription = stringResource(
                    R.string.cd_vehicle_photo_description, vehicle.make, vehicle.model
                ),
                statusChip = worstStatus?.let { s -> { AutoMinderServiceStatusBadge(status = s) } },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 2 — Odometer as a tappable instrument, not floating text.
        item(key = "odometer") {
            val formattedOdometer = remember(vehicle.currentOdometer, distanceUnit) {
                DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit))
            }
            val odometerInteractionSource = remember { MutableInteractionSource() }
            Surface(
                onClick = { showMileageSheet = true },
                interactionSource = odometerInteractionSource,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale(odometerInteractionSource)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.vehicle_detail_odometer),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$formattedOdometer ${DistanceUtil.unitLabel(distanceUnit)}",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = com.autominder.app.ui.theme.JetBrainsMono
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.vehicle_detail_tap_to_update),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.vehicle_detail_tap_to_update),
                            modifier = Modifier
                                .padding(10.dp)
                                .size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 3 — A calm verdict before any list of problems.
        item(key = "diagnosis") {
            if (reminders.isEmpty()) {
                // Nothing tracked yet — actionable setup card with a truthful CTA.
                ReminderSetupCard(
                    onAddReminderClick = onAddReminder,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                val attentionCount = remember(reminders, reminderStatuses) {
                    reminders.count {
                        (reminderStatuses[it.id] ?: ServiceStatus.UNKNOWN) in
                            setOf(ServiceStatus.OVERDUE, ServiceStatus.DUE_SOON)
                    }
                }
                val startWith = reminders
                    .filter { reminderStatuses[it.id] == ServiceStatus.OVERDUE }
                    .maxByOrNull { r ->
                        r.nextDueOdometer?.let { vehicle.currentOdometer - it } ?: Int.MIN_VALUE
                    }
                MaintenanceVerdictCard(
                    headlineText = if (attentionCount > 0) {
                        pluralStringResource(
                            R.plurals.dashboard_attention_headline, attentionCount, attentionCount
                        )
                    } else {
                        stringResource(R.string.dashboard_all_clear_headline)
                    },
                    supportingText = if (startWith != null) {
                        stringResource(
                            R.string.vehicle_detail_start_with,
                            startWith.customLabel ?: startWith.serviceType.localizedLabel()
                        )
                    } else if (attentionCount == 0) {
                        stringResource(R.string.dashboard_all_clear_supporting)
                    } else {
                        stringResource(R.string.dashboard_cockpit_supporting)
                    },
                    status = worstStatus,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Cost summary (Pro feature)
        if (totalCostCents > 0 || yearCostCents > 0) {
            item {
                ProFeatureGate(
                    isProUser = isProUser,
                    onUpgradeClick = onUpgradeClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.vehicle_detail_cost_this_year),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = VehicleDetailViewModel.formatCost(yearCostCents),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.vehicle_detail_cost_all_time),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = VehicleDetailViewModel.formatCost(totalCostCents),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }

                        if (costPerKmCents != null) {
                            val unit = LocalDistanceUnit.current
                            // Stored per km; scale up when displaying per mile
                            val perUnitCents = if (unit == "mi") costPerKmCents * 1.609344 else costPerKmCents
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(
                                    R.string.vehicle_detail_cost_per_km,
                                    VehicleDetailViewModel.formatCost(perUnitCents.toInt()),
                                    DistanceUtil.unitLabel(unit)
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (monthlySpending.any { it.cents > 0 }) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.chart_monthly_spending),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SpendingTrendChart(data = monthlySpending)
                        }

                        if (costByType.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.chart_cost_by_type),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CostByTypeDonut(data = costByType)
                        }
                    }
                }
                } // ProFeatureGate
            }
        }

        // Fuel Efficiency Card (Pro feature)
        if (averageEfficiency > 0) {
            item {
                ProFeatureGate(
                    isProUser = isProUser,
                    onUpgradeClick = onUpgradeClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.fuel_efficiency_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.fuel_efficiency_value_km_l, averageEfficiency),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = stringResource(R.string.fuel_efficiency_label),
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        if (efficiencySeries.size >= 2) {
                            Spacer(modifier = Modifier.height(12.dp))
                            FuelEfficiencyChart(series = efficiencySeries)
                        }
                    }
                }
                } // ProFeatureGate
            }
        }

        // 4 — One emphasized next step; Export lives in the app-bar overflow.
        item(key = "actions") {
            PremiumActionGrid(
                actions = listOf(
                    PremiumAction(
                        icon = Icons.Default.Build,
                        label = stringResource(R.string.vehicle_detail_log_service),
                        onClick = onAddServiceClick,
                        emphasized = true
                    ),
                    PremiumAction(
                        icon = Icons.Default.LocalGasStation,
                        label = stringResource(R.string.fuel_add_title),
                        onClick = onAddFuelClick
                    ),
                    PremiumAction(
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.vehicle_detail_mileage),
                        onClick = onMileageLogClick
                    ),
                    PremiumAction(
                        icon = Icons.Default.History,
                        label = stringResource(R.string.fuel_history_title),
                        onClick = onFuelHistoryClick
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (reminders.isEmpty()) {
            item(key = "no_reminders") {
                Text(
                    text = stringResource(R.string.vehicle_detail_no_reminders),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
        } else {
            // 5 — Triage, not a wall: worst three lead, the rest fold away,
            // nothing becomes unreachable. Faults never share a stream with
            // routine services.
            val visibleAttention = if (showAllAttention) {
                needsAttention
            } else {
                needsAttention.take(3)
            }

            if (needsAttention.isNotEmpty()) {
                item(key = "section_attention") {
                    PremiumSectionHeader(
                        title = stringResource(R.string.vehicle_detail_needs_attention),
                        countText = needsAttention.size.toString(),
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(visibleAttention, key = { it.id }) { reminder ->
                    val status = reminderStatuses[reminder.id] ?: ServiceStatus.UNKNOWN
                    val (timingPrimary, timingSecondary) = reminderTiming(
                        reminder = reminder,
                        status = status,
                        prediction = reminderPredictions[reminder.id],
                        currentOdometerKm = vehicle.currentOdometer,
                        distanceUnit = distanceUnit
                    )
                    StatusReminderCard(
                        title = reminder.customLabel ?: reminder.serviceType.localizedLabel(),
                        status = status,
                        timingPrimary = timingPrimary,
                        timingSecondary = timingSecondary,
                        doneLabel = stringResource(R.string.action_done),
                        snoozeLabel = stringResource(R.string.action_snooze),
                        editContentDescription = stringResource(R.string.action_edit),
                        onClick = { onReminderClick(reminder) },
                        onDone = { onMarkComplete(reminder.id) },
                        onSnooze = { onSnooze(reminder.id) },
                        onEdit = { onEditReminder(reminder.id) },
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp)
                    )
                }
                if (needsAttention.size > 3) {
                    item(key = "attention_expander") {
                        TextButton(
                            onClick = { showAllAttention = !showAllAttention },
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = if (showAllAttention) {
                                    stringResource(R.string.vehicle_detail_show_less)
                                } else {
                                    stringResource(
                                        R.string.vehicle_detail_show_all, needsAttention.size
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                // When nothing is wrong, say so in one calm sentence.
                item(key = "section_all_clear") {
                    val nextDue = upcoming
                        .mapNotNull { reminderPredictions[it.id]?.predictedAt }
                        .minOrNull()
                    AllClearBanner(
                        nextDueMillis = nextDue,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            if (upcoming.isNotEmpty()) {
                item(key = "section_upcoming") {
                    PremiumSectionHeader(
                        title = stringResource(R.string.vehicle_detail_upcoming),
                        countText = upcoming.size.toString(),
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(upcoming, key = { it.id }) { reminder ->
                    val status = reminderStatuses[reminder.id] ?: ServiceStatus.UNKNOWN
                    val (timingPrimary, timingSecondary) = reminderTiming(
                        reminder = reminder,
                        status = status,
                        prediction = reminderPredictions[reminder.id],
                        currentOdometerKm = vehicle.currentOdometer,
                        distanceUnit = distanceUnit
                    )
                    StatusReminderCard(
                        title = reminder.customLabel ?: reminder.serviceType.localizedLabel(),
                        status = status,
                        timingPrimary = timingPrimary,
                        timingSecondary = timingSecondary,
                        doneLabel = stringResource(R.string.action_done),
                        snoozeLabel = stringResource(R.string.action_snooze),
                        editContentDescription = stringResource(R.string.action_edit),
                        onClick = { onReminderClick(reminder) },
                        onDone = { onMarkComplete(reminder.id) },
                        onSnooze = { onSnooze(reminder.id) },
                        onEdit = { onEditReminder(reminder.id) },
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * Timing pair for a reminder row — primary line first, optional demoted
 * second line. Same truth contract as Slice 1A: a mileage-fired overdue
 * leads with "Overdue by X km"; a future date never leads on an overdue card.
 */
@Composable
private fun reminderTiming(
    reminder: Reminder,
    status: ServiceStatus,
    prediction: DuePrediction?,
    currentOdometerKm: Int,
    distanceUnit: String
): Pair<String, String?> {
    val kmLeft = prediction?.kmRemaining
    val expectedAt = prediction?.predictedAt
    val overdueByMileage = status == ServiceStatus.OVERDUE &&
        reminder.nextDueOdometer != null &&
        currentOdometerKm >= reminder.nextDueOdometer
    return when {
        overdueByMileage -> {
            overdueByText(
                overdueKm = currentOdometerKm - reminder.nextDueOdometer!!,
                intervalKm = reminder.intervalKm,
                distanceUnit = distanceUnit
            ) to reminder.nextDueDate?.let {
                stringResource(R.string.vehicle_detail_due_date, DateFormatUtil.formatDate(it))
            }
        }
        kmLeft != null && expectedAt != null -> {
            val left = remember(kmLeft, distanceUnit) {
                DistanceFormat.grouped(DistanceUtil.kmToDisplay(kmLeft, distanceUnit))
            }
            stringResource(
                R.string.vehicle_detail_forecast,
                left,
                DistanceUtil.unitLabel(distanceUnit),
                DateFormatUtil.formatDate(expectedAt)
            ) to null
        }
        reminder.nextDueDate != null -> stringResource(
            R.string.vehicle_detail_due_date, DateFormatUtil.formatDate(reminder.nextDueDate)
        ) to reminder.nextDueOdometer?.let {
            stringResource(
                R.string.vehicle_detail_due_at_dynamic,
                DistanceFormat.grouped(DistanceUtil.kmToDisplay(it, distanceUnit)),
                DistanceUtil.unitLabel(distanceUnit)
            )
        }
        reminder.nextDueOdometer != null -> stringResource(
            R.string.vehicle_detail_due_at_dynamic,
            DistanceFormat.grouped(DistanceUtil.kmToDisplay(reminder.nextDueOdometer, distanceUnit)),
            DistanceUtil.unitLabel(distanceUnit)
        ) to null
        else -> stringResource(R.string.reminder_detail_timing_unset) to null
    }
}

@Composable
private fun AllClearBanner(
    nextDueMillis: Long?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (nextDueMillis != null) {
                stringResource(R.string.vehicle_detail_all_clear_until, DateFormatUtil.formatDate(nextDueMillis))
            } else {
                stringResource(R.string.vehicle_detail_all_clear)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Shown instead of the verdict card when the vehicle has zero reminders:
 * an honest setup nudge whose CTA actually opens Add Reminder.
 */
@Composable
private fun ReminderSetupCard(
    onAddReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                // Decorative: the title below already carries the meaning.
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.vehicle_reminders_setup_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.vehicle_reminders_setup_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onAddReminderClick) {
                Text(stringResource(R.string.vehicle_reminders_add_reminder))
            }
        }
    }
}
