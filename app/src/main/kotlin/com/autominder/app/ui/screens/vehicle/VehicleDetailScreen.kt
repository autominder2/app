package com.autominder.app.ui.screens.vehicle

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.LocalIsProUser
import com.autominder.app.R
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.usecase.DuePrediction
import com.autominder.app.core.util.labelRes
import com.autominder.app.domain.usecase.cockpit.ConfidenceFactor
import com.autominder.app.domain.usecase.cockpit.ConfidenceSignal
import com.autominder.app.domain.usecase.cockpit.ConfidenceState
import com.autominder.app.domain.usecase.cockpit.DrivingPattern
import com.autominder.app.domain.usecase.cockpit.OwnershipCostSummary
import com.autominder.app.domain.usecase.cockpit.VehicleConfidence
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.domain.util.VehicleBodyTypeResolver
import com.autominder.app.domain.util.VehicleDisplayNameFormatter
import com.autominder.app.ui.components.AutoMinderServiceStatusBadge
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.ListSkeleton
import com.autominder.app.ui.components.LocalSnackbarHostState
import com.autominder.app.ui.components.ProFeatureGate
import com.autominder.app.ui.components.QuickMileageSheet
import com.autominder.app.ui.components.ReminderDetailSheet
import com.autominder.app.ui.components.charts.CostByTypeDonut
import com.autominder.app.ui.components.charts.FuelEfficiencyChart
import com.autominder.app.ui.components.charts.SpendingTrendChart
import com.autominder.app.ui.components.premium.MaintenanceVerdictCard
import com.autominder.app.ui.components.premium.PremiumAction
import com.autominder.app.ui.components.premium.PremiumActionGrid
import com.autominder.app.ui.components.premium.PremiumSectionHeader
import com.autominder.app.ui.components.premium.StatusReminderCard
import com.autominder.app.ui.components.premium.VehicleHeroCard
import com.autominder.app.ui.components.premium.VehicleHeroVariant
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel
import com.autominder.app.ui.util.overdueByText
import kotlinx.coroutines.launch

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
    val showBarTitle by remember {
        derivedStateOf {
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
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.vehicle_detail_export)) },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.onEvent(VehicleDetailUiEvent.ExportClicked)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_archive)) },
                                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showArchiveDialog = true
                                }
                            )
                        }

                        if (showArchiveDialog) {
                            AlertDialog(
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
                        confidence = uiState.confidence,
                        drivingPattern = uiState.drivingPattern,
                        costSummary = uiState.costSummary,
                        recentServices = uiState.recentServices,
                        averageEfficiency = uiState.averageEfficiency,
                        efficiencySeries = uiState.efficiencySeries,
                        isProUser = isProUser,
                        onAddServiceClick = { onNavigateToAddService(v.id) },
                        onMileageLogClick = { onNavigateToMileageLog(v.id) },
                        onAddFuelClick = { onNavigateToAddFuel(v.id) },
                        onFuelHistoryClick = { onNavigateToFuelHistory(v.id) },
                        onExportPassportClick = {
                            if (isProUser) {
                                viewModel.onEvent(VehicleDetailUiEvent.ExportClicked)
                            } else {
                                onNavigateToPaywall()
                            }
                        },
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
    confidence: VehicleConfidence,
    drivingPattern: DrivingPattern,
    costSummary: OwnershipCostSummary,
    recentServices: List<Service>,
    averageEfficiency: Double,
    efficiencySeries: List<Double>,
    isProUser: Boolean,
    onAddServiceClick: () -> Unit,
    onMileageLogClick: () -> Unit,
    onAddFuelClick: () -> Unit,
    onFuelHistoryClick: () -> Unit,
    onExportPassportClick: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onMarkComplete: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onEditReminder: (Long) -> Unit,
    onUpdateOdometer: (Int) -> Unit
) {
    var showMileageSheet by rememberSaveable { mutableStateOf(false) }
    var showConfidenceSheet by rememberSaveable { mutableStateOf(false) }
    var consumedMileageRequestId by rememberSaveable { mutableLongStateOf(Long.MIN_VALUE) }

    LaunchedEffect(autoOpenMileageSheet, mileageRequestId) {
        if (autoOpenMileageSheet && consumedMileageRequestId != mileageRequestId) {
            consumedMileageRequestId = mileageRequestId
            showMileageSheet = true
        }
    }

    var showAllAttention by remember { mutableStateOf(false) }
    val mileageSheetState = rememberModalBottomSheetState()
    val confidenceSheetState = rememberModalBottomSheetState()
    val distanceUnit = LocalDistanceUnit.current
    val haptic = LocalHapticFeedback.current
    val onUpgradeClick: () -> Unit = onNavigateToPaywall

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

    if (showConfidenceSheet) {
        ExplainableConfidenceSheet(
            confidence = confidence,
            onDismiss = { showConfidenceSheet = false }
        )
    }

    val worstStatus = remember(reminders, reminderStatuses) {
        reminders
            .mapNotNull { reminderStatuses[it.id] }
            .maxByOrNull { it.severity }
    }

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1 — SECTION 1: VEHICLE IDENTITY HERO
        item(key = "hero") {
            val bodyType = remember(vehicle.make, vehicle.model) {
                VehicleBodyTypeResolver.resolve(vehicle.make, vehicle.model)
            }
            VehicleHeroCard(
                title = VehicleDisplayNameFormatter.format(vehicle.make, vehicle.model, vehicle.year),
                variant = VehicleHeroVariant.Expanded,
                yearText = vehicle.year.takeIf { it > 0 }?.toString(),
                photoUri = vehicle.photoUri,
                photoContentDescription = stringResource(
                    R.string.cd_vehicle_photo_description, vehicle.make, vehicle.model
                ),
                statusChip = worstStatus?.let { s -> { AutoMinderServiceStatusBadge(status = s) } },
                bodyType = bodyType,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 2 — SECTION 2: EXPLAINABLE VEHICLE CONFIDENCE BENTO (Tappable)
        item(key = "confidence_bento") {
            VehicleConfidenceCard(
                confidence = confidence,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showConfidenceSheet = true
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 3 — SECTION 3: 1-TAP QUICK ACTION DOCK
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

        // 4 — SECTION 4: 2-COLUMN TELEMETRY BENTO DECK (Pace + Cost)
        item(key = "bento_telemetry_deck") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pace & Wear Card
                val dailyKmRate = drivingPattern.dailyKmRate
                val displayPace = dailyKmRate?.let { pace ->
                    if (distanceUnit == "mi") pace * 0.621371 else pace
                }
                val paceValue = if (displayPace != null) {
                    stringResource(
                        R.string.vehicle_bento_wear_pace,
                        displayPace,
                        DistanceUtil.unitLabel(distanceUnit)
                    )
                } else {
                    "~35.0 ${DistanceUtil.unitLabel(distanceUnit)} / day"
                }
                BentoStatCard(
                    title = stringResource(R.string.vehicle_bento_wear_trajectory),
                    value = paceValue,
                    subtitle = drivingPattern.paceLabel,
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )

                // Cost Telemetry Card
                BentoStatCard(
                    title = stringResource(R.string.vehicle_bento_cost_title),
                    value = VehicleDetailViewModel.formatCost(costSummary.totalCostCents),
                    subtitle = stringResource(R.string.vehicle_bento_cost_total),
                    icon = Icons.Default.DirectionsCar,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Odometer Instrument
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
                                fontFamily = JetBrainsMono
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
                        shape = CircleShape,
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

        // Maintenance Verdict & Priority Action
        item(key = "diagnosis") {
            if (reminders.isEmpty()) {
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

        // Cost Analysis Breakdown (Pro)
        if (costSummary.totalCostCents > 0 || costSummary.yearCostCents > 0) {
            item(key = "cost_pro_deck") {
                ProFeatureGate(
                    isProUser = isProUser,
                    onUpgradeClick = onUpgradeClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
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
                                        text = VehicleDetailViewModel.formatCost(costSummary.yearCostCents),
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
                                        text = VehicleDetailViewModel.formatCost(costSummary.totalCostCents),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }

                            if (costSummary.costPerKmCents != null) {
                                val unit = LocalDistanceUnit.current
                                val perUnitCents = if (unit == "mi") costSummary.costPerKmCents * 1.609344 else costSummary.costPerKmCents
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

                            if (costSummary.monthlySpending.any { it.cents > 0 }) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.chart_monthly_spending),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SpendingTrendChart(data = costSummary.monthlySpending)
                            }

                            if (costSummary.costByType.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.chart_cost_by_type),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CostByTypeDonut(data = costSummary.costByType)
                            }
                        }
                    }
                }
            }
        }

        // Fuel Efficiency Card (Pro)
        if (averageEfficiency > 0) {
            item(key = "fuel_efficiency_deck") {
                ProFeatureGate(
                    isProUser = isProUser,
                    onUpgradeClick = onUpgradeClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
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
                }
            }
        }

        // SECTION 5: MAINTENANCE & HISTORY TIMELINE
        if (reminders.isNotEmpty()) {
            val visibleAttention = if (showAllAttention) needsAttention else needsAttention.take(3)

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

            // Upcoming Stream
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

        // Recent Service History Records
        item(key = "recent_services_bento") {
            RecentServicesBentoCard(
                services = recentServices,
                distanceUnit = distanceUnit,
                onAddServiceClick = onAddServiceClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Certified Vehicle Passport Banner (Pro Anchor)
        item(key = "certified_passport_banner") {
            CertifiedPassportBanner(
                isProUser = isProUser,
                onExportClick = onExportPassportClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * Semantic colour for a maintenance state, per the contract in `Color.kt`:
 * tertiary = healthy, secondary = due soon, error = past due. `primary` is the
 * brand/action slot and deliberately never encodes a maintenance state.
 *
 * Colour is never the only signal here - the chip carries a text label and the
 * icon differs per state, so the card survives a colour-blind reader and a
 * greyscale screenshot.
 */
@Composable
private fun ConfidenceState.accentColor(): Color = when (this) {
    ConfidenceState.UP_TO_DATE -> MaterialTheme.colorScheme.tertiary
    ConfidenceState.DUE_SOON -> MaterialTheme.colorScheme.secondary
    ConfidenceState.OVERDUE -> MaterialTheme.colorScheme.error
    ConfidenceState.NEEDS_SETUP -> MaterialTheme.colorScheme.onSurfaceVariant
}

@StringRes
private fun ConfidenceState.chipLabelRes(): Int = when (this) {
    ConfidenceState.UP_TO_DATE -> R.string.vehicle_confidence_state_ok
    ConfidenceState.DUE_SOON -> R.string.vehicle_confidence_state_due_soon
    ConfidenceState.OVERDUE -> R.string.vehicle_confidence_state_overdue
    ConfidenceState.NEEDS_SETUP -> R.string.vehicle_confidence_state_setup
}

@StringRes
private fun ConfidenceState.headlineRes(): Int = when (this) {
    ConfidenceState.UP_TO_DATE -> R.string.vehicle_confidence_headline_ok
    ConfidenceState.DUE_SOON -> R.string.vehicle_confidence_headline_due_soon
    ConfidenceState.OVERDUE -> R.string.vehicle_confidence_headline_overdue
    ConfidenceState.NEEDS_SETUP -> R.string.vehicle_confidence_headline_setup
}

private fun ConfidenceState.icon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    ConfidenceState.UP_TO_DATE -> Icons.Default.CheckCircle
    ConfidenceState.DUE_SOON -> Icons.Default.Schedule
    ConfidenceState.OVERDUE -> Icons.Default.Warning
    ConfidenceState.NEEDS_SETUP -> Icons.Default.Info
}

@Composable
private fun confidenceSummary(confidence: VehicleConfidence): String = when (confidence.state) {
    ConfidenceState.NEEDS_SETUP -> stringResource(R.string.vehicle_confidence_summary_setup)
    ConfidenceState.UP_TO_DATE -> stringResource(R.string.vehicle_confidence_summary_ok)
    ConfidenceState.DUE_SOON -> pluralStringResource(
        R.plurals.vehicle_confidence_summary_due_soon,
        confidence.dueSoonCount,
        confidence.dueSoonCount
    )
    ConfidenceState.OVERDUE -> pluralStringResource(
        R.plurals.vehicle_confidence_summary_overdue,
        confidence.overdueCount,
        confidence.overdueCount
    )
}

@Composable
private fun nextItemLine(confidence: VehicleConfidence): String? {
    val typeLabel = confidence.nextServiceType?.let { stringResource(it.labelRes()) }
    val label = confidence.nextCustomLabel ?: typeLabel ?: return null
    return stringResource(R.string.vehicle_confidence_next, label)
}

@Composable
private fun factorTitle(factor: ConfidenceFactor): String {
    val typeLabel = factor.serviceType?.let { stringResource(it.labelRes()) }
    val itemLabel = factor.customLabel ?: typeLabel.orEmpty()
    return when (factor.signal) {
        ConfidenceSignal.SCHEDULE_ACTIVE -> pluralStringResource(
            R.plurals.vehicle_confidence_signal_schedule, factor.count, factor.count
        )
        ConfidenceSignal.NO_SCHEDULE ->
            stringResource(R.string.vehicle_confidence_signal_no_schedule)
        ConfidenceSignal.RECORDS_ON_FILE -> pluralStringResource(
            R.plurals.vehicle_confidence_signal_records, factor.count, factor.count
        )
        ConfidenceSignal.NO_RECORDS ->
            stringResource(R.string.vehicle_confidence_signal_no_records)
        ConfidenceSignal.ODOMETER_RECENT -> pluralStringResource(
            R.plurals.vehicle_confidence_signal_odometer_recent, factor.count, factor.count
        )
        ConfidenceSignal.ODOMETER_STALE -> pluralStringResource(
            R.plurals.vehicle_confidence_signal_odometer_stale, factor.count, factor.count
        )
        ConfidenceSignal.NOTHING_OVERDUE ->
            stringResource(R.string.vehicle_confidence_signal_nothing_overdue)
        ConfidenceSignal.ITEM_OVERDUE ->
            stringResource(R.string.vehicle_confidence_signal_overdue_item, itemLabel)
        ConfidenceSignal.ITEM_DUE_SOON ->
            stringResource(R.string.vehicle_confidence_signal_due_soon_item, itemLabel)
    }
}

@Composable
private fun factorDetail(factor: ConfidenceFactor): String? = when (factor.signal) {
    ConfidenceSignal.NO_SCHEDULE ->
        stringResource(R.string.vehicle_confidence_signal_no_schedule_detail)
    ConfidenceSignal.NO_RECORDS ->
        stringResource(R.string.vehicle_confidence_signal_no_records_detail)
    ConfidenceSignal.ODOMETER_STALE ->
        stringResource(R.string.vehicle_confidence_signal_odometer_stale_detail)
    else -> null
}

/**
 * Maintenance status card.
 *
 * Replaced an animated 0-100 score ring on 2026-08-26. The card now states the
 * worst real status among the reminders the owner set, and nothing more - no
 * percentage, no verdict, no progress bar, because there is no measurement
 * behind any of them.
 */
@Composable
private fun VehicleConfidenceCard(
    confidence: VehicleConfidence,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = confidence.state.accentColor()
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = confidence.state.icon(),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(confidence.state.headlineRes()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(confidence.state.chipLabelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = confidenceSummary(confidence),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            nextItemLine(confidence)?.let { next ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = next,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.vehicle_confidence_tap_to_explain),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * The receipt behind the card: every observation, plus the plain statement that
 * AutoMinder has never inspected the vehicle. This sheet used to lead with
 * "Why 87%?" and a "Record Completeness" bar; both went with the score.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplainableConfidenceSheet(
    confidence: VehicleConfidence,
    onDismiss: () -> Unit
) {
    val accent = confidence.state.accentColor()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.vehicle_confidence_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Exo2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(confidence.state.chipLabelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.vehicle_confidence_sheet_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.vehicle_confidence_factors_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            confidence.factors.forEach { factor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (factor.isPositive) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = if (factor.isPositive) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Column {
                        Text(
                            text = factorTitle(factor),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        factorDetail(factor)?.let { detail ->
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.action_ok))
            }
        }
    }
}

@Composable
private fun BentoStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentServicesBentoCard(
    services: List<Service>,
    distanceUnit: String,
    onAddServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.vehicle_bento_recent_services),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = onAddServiceClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.vehicle_detail_log_service),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (services.isEmpty()) {
                Text(
                    text = stringResource(R.string.vehicle_bento_no_recent_services),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                services.forEach { service ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = service.customLabel ?: service.serviceType.localizedLabel(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = DateFormatUtil.formatDate(service.serviceDate) +
                                    (service.odometerAtService?.let { " • ${DistanceFormat.grouped(DistanceUtil.kmToDisplay(it, distanceUnit))} ${DistanceUtil.unitLabel(distanceUnit)}" } ?: ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        service.costCents?.let { cost ->
                            if (cost > 0) {
                                Text(
                                    text = VehicleDetailViewModel.formatCost(cost),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMono
                                    ),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CertifiedPassportBanner(
    isProUser: Boolean,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.vehicle_bento_passport_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.vehicle_bento_passport_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onExportClick,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.vehicle_bento_passport_cta))
                }
            }
        }
    }
}

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
