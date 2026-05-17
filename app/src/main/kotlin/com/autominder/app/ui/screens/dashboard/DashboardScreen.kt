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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@Composable
fun DashboardScreen(
    onNavigateToVehicleDetail: (Long) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val fabExtended by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        topBar = {
            DashboardTopBar()
        },
        floatingActionButton = {
            val haptic = LocalHapticFeedback.current
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToAddVehicle()
                },
                expanded = fabExtended,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.action_add_vehicle)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_top_bar_title),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dashboard_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
