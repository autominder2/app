package com.autominder.app.ui.screens.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.autominder.app.ui.components.StatusChip
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.LocalDistanceUnit
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

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

    val vehicles = (uiState as? DashboardUiState.Success)?.vehicles ?: emptyList()
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<QuickAction?>(null) }

    // One vehicle: go straight there. Several: ask which one.
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
                // Quick actions revealed above the main FAB
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
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> LoadingState()
                is DashboardUiState.Empty -> EmptyState(
                    title = stringResource(R.string.dashboard_no_vehicles_title),
                    subtitle = stringResource(R.string.dashboard_no_vehicles_subtitle),
                    onAction = onNavigateToAddVehicle,
                    actionLabel = stringResource(R.string.action_add_vehicle),
                    icon = Icons.Default.Commute,
                    hint = stringResource(R.string.dashboard_add_first_vehicle_hint)
                )
                is DashboardUiState.Error -> ErrorState(
                    message = state.message ?: stringResource(R.string.dashboard_error),
                    onRetry = { viewModel.retry() }
                )
                is DashboardUiState.Success -> DashboardContent(
                    vehicles = state.vehicles,
                    onVehicleClick = onNavigateToVehicleDetail,
                    listState = listState
                )
            }
        }
    }
}

/** Labeled mini-FAB row used by the expanded quick-actions menu. */
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
    onVehicleClick: (Long) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "health_score") {
            FleetHealthScore(
                vehicles = vehicles,
                modifier = Modifier.animateItem()
            )
        }
        items(vehicles, key = { it.vehicle.id }) { vehicleWithStatus ->
            VehicleCard(
                vehicleWithStatus = vehicleWithStatus,
                onClick = { onVehicleClick(vehicleWithStatus.vehicle.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun VehicleCard(
    vehicleWithStatus: VehicleWithStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicle = vehicleWithStatus.vehicle

    val targetCornerDp = when (vehicleWithStatus.status) {
        ServiceStatus.OVERDUE -> 8f
        ServiceStatus.DUE_SOON -> 16f
        else -> 28f
    }
    val cornerRadius by animateFloatAsState(
        targetValue = targetCornerDp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardCorner"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(cornerRadius.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Hero photo strip — full width when photo exists
            if (vehicle.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(vehicle.photoUri)
                        .crossfade(300)
                        .build(),
                    contentDescription = stringResource(R.string.cd_vehicle_photo_description, vehicle.make, vehicle.model),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Only show icon placeholder when there's no hero photo
                if (vehicle.photoUri == null) {
                    Card(
                        modifier = Modifier.size(40.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Commute,
                            contentDescription = stringResource(R.string.cd_vehicle_no_photo),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${vehicle.year} • ${DistanceUtil.kmToDisplay(vehicle.currentOdometer, LocalDistanceUnit.current)} ${DistanceUtil.unitLabel(LocalDistanceUnit.current)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (vehicleWithStatus.overdueCount > 0 || vehicleWithStatus.dueSoonCount > 0) {
                        val overdueText = if (vehicleWithStatus.overdueCount > 0) stringResource(R.string.dashboard_overdue_count, vehicleWithStatus.overdueCount) else ""
                        val dueSoonText = if (vehicleWithStatus.dueSoonCount > 0) stringResource(R.string.dashboard_due_soon_count, vehicleWithStatus.dueSoonCount) else ""
                        val alertText = buildString {
                            if (overdueText.isNotEmpty()) append(overdueText)
                            if (overdueText.isNotEmpty() && dueSoonText.isNotEmpty()) append(", ")
                            if (dueSoonText.isNotEmpty()) append(dueSoonText)
                        }
                        Text(
                            text = alertText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (vehicleWithStatus.overdueCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusChip(status = vehicleWithStatus.status)
            }
        }
    }
}
