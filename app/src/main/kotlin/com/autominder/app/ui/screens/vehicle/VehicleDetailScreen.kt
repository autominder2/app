package com.autominder.app.ui.screens.vehicle

import androidx.compose.animation.AnimatedContent
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
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.LocalIsProUser
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.LoadingState
import com.autominder.app.ui.components.ProFeatureGate
import com.autominder.app.ui.components.QuickMileageSheet
import com.autominder.app.ui.components.StatusChip
import com.autominder.app.ui.util.DateFormatUtil
import androidx.compose.material3.SnackbarDuration
import com.autominder.app.ui.components.LocalSnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddReminder: (Long) -> Unit,
    onNavigateToEditVehicle: (Long) -> Unit = {},
    onNavigateToAddService: (Long) -> Unit = {},
    onNavigateToMileageLog: (Long) -> Unit = {},
    onNavigateToEditReminder: (Long) -> Unit = {},
    onNavigateToAddFuel: (Long) -> Unit = {},
    onNavigateToFuelHistory: (Long) -> Unit = {},
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(uiState.vehicle?.let { stringResource(R.string.vehicle_make_model, it.make, it.model) } ?: stringResource(R.string.vehicle_detail_title)) },
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
                        var showArchiveDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                        IconButton(onClick = { showArchiveDialog = true }) {
                            Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.action_archive))
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
                ScreenState.Loading -> LoadingState()
                ScreenState.Empty -> EmptyState(
                    title = stringResource(R.string.vehicle_detail_not_found),
                    subtitle = stringResource(R.string.vehicle_detail_not_found_subtitle)
                )
                ScreenState.Error -> ErrorState(
                    message = uiState.error ?: stringResource(R.string.vehicle_detail_unknown_error),
                    onRetry = { viewModel.retry() }
                )
                ScreenState.Success -> uiState.vehicle?.let { v ->
                    VehicleDetailContent(
                        vehicle = v,
                        reminders = uiState.reminders,
                        reminderStatuses = uiState.reminderStatuses,
                        totalCostCents = uiState.totalCostCents,
                        yearCostCents = uiState.yearCostCents,
                        averageEfficiency = uiState.averageEfficiency,
                        isProUser = isProUser,
                        onAddServiceClick = { onNavigateToAddService(v.id) },
                        onMileageLogClick = { onNavigateToMileageLog(v.id) },
                        onAddFuelClick = { onNavigateToAddFuel(v.id) },
                        onFuelHistoryClick = { onNavigateToFuelHistory(v.id) },
                        onExportClick = { viewModel.onEvent(VehicleDetailUiEvent.ExportClicked) },
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
                uiState.error != null -> Error
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
    reminders: List<Reminder>,
    reminderStatuses: Map<Long, ServiceStatus>,
    totalCostCents: Int,
    yearCostCents: Int,
    averageEfficiency: Double,
    isProUser: Boolean,
    onAddServiceClick: () -> Unit,
    onMileageLogClick: () -> Unit,
    onAddFuelClick: () -> Unit,
    onFuelHistoryClick: () -> Unit,
    onExportClick: () -> Unit,
    onMarkComplete: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onEditReminder: (Long) -> Unit,
    onUpdateOdometer: (Int) -> Unit
) {
    var showMileageSheet by remember { mutableStateOf(false) }
    val mileageSheetState = rememberModalBottomSheetState()
    val distanceUnit = LocalDistanceUnit.current
    val haptic = LocalHapticFeedback.current

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Vehicle header — hero photo or icon fallback
        item {
            if (vehicle.photoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(vehicle.photoUri)
                            .crossfade(300)
                            .build(),
                        contentDescription = stringResource(R.string.cd_vehicle_photo_description, vehicle.make, vehicle.model),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient scrim for legible text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                    startY = 60f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.vehicle_detail_year, vehicle.year),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = stringResource(R.string.cd_vehicle_no_photo),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = stringResource(R.string.vehicle_detail_year, vehicle.year),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Odometer — tap to update via quick sheet
        item {
            Surface(
                onClick = { showMileageSheet = true },
                color = androidx.compose.ui.graphics.Color.Transparent,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.vehicle_detail_odometer), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.vehicle_detail_tap_to_update),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${DistanceUtil.kmToDisplay(vehicle.currentOdometer, LocalDistanceUnit.current)} ${DistanceUtil.unitLabel(LocalDistanceUnit.current)}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.vehicle_detail_tap_to_update),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Vehicle health score
        item {
            val vehicle = vehicle
            val score = computeHealthScore(reminders, reminderStatuses)
            HealthScoreCard(
                score = score,
                onAddReminderClick = { onAddServiceClick() }, // Repurpose the reminder add action
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Cost summary (Pro feature)
        if (totalCostCents > 0 || yearCostCents > 0) {
            item {
                ProFeatureGate(
                    isProUser = isProUser,
                    onUpgradeClick = {},
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
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
                }
                } // ProFeatureGate
            }
        }

        // Fuel Efficiency Card (Pro feature)
        if (averageEfficiency > 0) {
            item {
                ProFeatureGate(
                    isProUser = isProUser,
                    onUpgradeClick = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
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
                }
                } // ProFeatureGate
            }
        }

        // Action buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddServiceClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = stringResource(R.string.vehicle_detail_log_service), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.vehicle_detail_log_service))
                    }
                    OutlinedButton(
                        onClick = onMileageLogClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = stringResource(R.string.vehicle_detail_mileage), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.vehicle_detail_mileage))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddFuelClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = stringResource(R.string.fuel_add_title), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.fuel_add_title))
                    }
                    OutlinedButton(
                        onClick = onFuelHistoryClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = stringResource(R.string.fuel_history_title), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.fuel_history_title))
                    }
                }
                OutlinedButton(
                    onClick = onExportClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.vehicle_detail_export), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.vehicle_detail_export))
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(R.string.vehicle_detail_reminders),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (reminders.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.vehicle_detail_no_reminders),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    status = reminderStatuses[reminder.id] ?: ServiceStatus.UNKNOWN,
                    onMarkComplete = { onMarkComplete(reminder.id) },
                    onSnooze = { onSnooze(reminder.id) },
                    onEdit = { onEditReminder(reminder.id) },
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    status: ServiceStatus,
    onMarkComplete: () -> Unit,
    onSnooze: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val cardColor by animateColorAsState(
        targetValue = when (status) {
            ServiceStatus.OVERDUE  -> MaterialTheme.colorScheme.errorContainer
            ServiceStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiaryContainer
            ServiceStatus.SNOOZED  -> MaterialTheme.colorScheme.surfaceVariant
            else                   -> MaterialTheme.colorScheme.surface
        },
        label = "cardColor"
    )
    val contentColor = when (status) {
        ServiceStatus.OVERDUE  -> MaterialTheme.colorScheme.onErrorContainer
        ServiceStatus.DUE_SOON -> MaterialTheme.colorScheme.onTertiaryContainer
        else                   -> MaterialTheme.colorScheme.onSurface
    }
    val elevation = if (status == ServiceStatus.OVERDUE) 4.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 200,
                    easing = androidx.compose.animation.core.EaseOutCubic
                )
            )
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Left accent strip for OVERDUE — IntrinsicSize.Min gives the Row a
                // bounded height so fillMaxHeight() on this strip resolves correctly
                if (status == ServiceStatus.OVERDUE) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.error)
                    )
                }
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reminder.customLabel ?: reminder.serviceType.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_edit),
                                modifier = Modifier.size(18.dp),
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                        }
                        StatusChip(status = status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (reminder.nextDueDate != null) {
                        Text(
                            text = stringResource(R.string.vehicle_detail_due_date, DateFormatUtil.formatDate(reminder.nextDueDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.75f)
                        )
                    }
                    if (reminder.nextDueOdometer != null) {
                        Text(
                            text = stringResource(R.string.vehicle_detail_due_at_dynamic, DistanceUtil.kmToDisplay(reminder.nextDueOdometer, LocalDistanceUnit.current).toString(), DistanceUtil.unitLabel(LocalDistanceUnit.current)),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.75f)
                        )
                    }

                    if (!reminder.isCompleted) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                onSnooze()
                            }) {
                                Text(stringResource(R.string.action_snooze), color = contentColor)
                            }
                            TextButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                onMarkComplete()
                            }) {
                                Text(stringResource(R.string.action_done), color = contentColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun computeHealthScore(
    reminders: List<Reminder>,
    statuses: Map<Long, ServiceStatus>
): Int {
    if (reminders.isEmpty()) return -1 // Sentinel: no reminders → actionable empty state
    val weights = mapOf(
        ServiceStatus.OVERDUE   to 0,
        ServiceStatus.DUE_SOON  to 50,
        ServiceStatus.SNOOZED   to 70,
        ServiceStatus.OK        to 100,
        ServiceStatus.UNKNOWN   to 100
    )
    val total = reminders.sumOf { weights[statuses[it.id] ?: ServiceStatus.UNKNOWN] ?: 100 }
    return (total / reminders.size).coerceIn(0, 100)
}

@Composable
private fun HealthScoreCard(
    score: Int,
    onAddReminderClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (score == -1) {
        // Actionable empty state — nudge user to set up health tracking
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
                    contentDescription = stringResource(R.string.cd_vehicle_health),
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.vehicle_health_setup_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.vehicle_health_setup_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (onAddReminderClick != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onAddReminderClick) {
                        Text(stringResource(R.string.vehicle_health_add_reminder))
                    }
                }
            }
        }
        return
    }

    val (color, label) = when {
        score >= 80 -> MaterialTheme.colorScheme.primary to stringResource(R.string.vehicle_health_great)
        score >= 60 -> MaterialTheme.colorScheme.tertiary to stringResource(R.string.vehicle_health_good)
        score >= 40 -> MaterialTheme.colorScheme.secondary to stringResource(R.string.vehicle_health_fair)
        else        -> MaterialTheme.colorScheme.error to stringResource(R.string.vehicle_health_needs_attention)
    }
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.vehicle_health_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    color = color,
                    trackColor = color.copy(alpha = 0.15f),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
