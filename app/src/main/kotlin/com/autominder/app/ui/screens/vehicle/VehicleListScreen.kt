package com.autominder.app.ui.screens.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.domain.util.VehicleDisplayNameFormatter
import com.autominder.app.ui.components.AutoMinderServiceStatusBadge
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.ListSkeleton
import com.autominder.app.ui.components.premium.VehicleHeroCard
import com.autominder.app.ui.components.premium.VehicleHeroVariant
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    onNavigateToVehicleDetail: (Long) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    viewModel: VehicleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.garage_hero_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            val haptic = LocalHapticFeedback.current
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToAddVehicle()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_vehicle))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is VehicleListUiState.Loading -> ListSkeleton()
                is VehicleListUiState.Empty -> EmptyState(
                    title = stringResource(R.string.dashboard_empty_title),
                    subtitle = stringResource(R.string.dashboard_empty_subtitle),
                    onAction = onNavigateToAddVehicle,
                    actionLabel = stringResource(R.string.action_add_vehicle)
                )
                is VehicleListUiState.Error -> ErrorState(
                    message = stringResource(state.messageRes),
                    onRetry = { viewModel.retry() }
                )
                is VehicleListUiState.Success -> VehicleListContent(
                    state = state,
                    onVehicleClick = onNavigateToVehicleDetail
                )
            }
        }
    }
}

@Composable
private fun VehicleListContent(
    state: VehicleListUiState.Success,
    onVehicleClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Fleet Health Command Summary Hero Card
        item(key = "fleet_summary_hero") {
            FleetHeroSummaryCard(
                totalVehicles = state.totalVehiclesCount,
                healthyCount = state.healthyCount,
                attentionCount = state.attentionCount,
                urgentVehicleName = state.fleetUrgentVehicleName,
                urgentReminderLabel = state.fleetUrgentReminderLabel,
                urgentVehicleId = state.fleetUrgentVehicleId,
                onUrgentClick = { state.fleetUrgentVehicleId?.let(onVehicleClick) }
            )
        }

        // 2. Individual Vehicle Command Cards
        itemsIndexed(state.items, key = { _, item -> item.vehicle.id }) { index, item ->
            VehicleListRow(
                item = item,
                isHero = index == 0,
                onClick = { onVehicleClick(item.vehicle.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun FleetHeroSummaryCard(
    totalVehicles: Int,
    healthyCount: Int,
    attentionCount: Int,
    urgentVehicleName: String?,
    urgentReminderLabel: String?,
    urgentVehicleId: Long?,
    onUrgentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStrictlyHealthy = attentionCount == 0 && healthyCount == totalVehicles && totalVehicles > 0
    val hasNoAttention = attentionCount == 0

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (hasNoAttention) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            isStrictlyHealthy -> stringResource(R.string.garage_all_healthy, totalVehicles)
                            hasNoAttention -> "$healthyCount of $totalVehicles vehicles up to date"
                            else -> stringResource(R.string.garage_attention_needed, attentionCount, totalVehicles)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            isStrictlyHealthy -> "All systems checked & up to date"
                            hasNoAttention -> "Add reminders to track remaining vehicles"
                            else -> "Scheduled maintenance requires attention"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Fleet Health Badge Pill
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (hasNoAttention) Color(0xFFE6F4EA) else Color(0xFFFEE4E2)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasNoAttention) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasNoAttention) Color(0xFF167A55) else Color(0xFFB42318),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = when {
                                isStrictlyHealthy -> "All Good"
                                hasNoAttention -> "Up to Date"
                                else -> "$attentionCount Alert"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasNoAttention) Color(0xFF167A55) else Color(0xFFB42318)
                        )
                    }
                }
            }

            // Urgent action teaser banner if attention is required
            if (!hasNoAttention && urgentVehicleName != null && urgentReminderLabel != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = urgentVehicleId != null, onClick = onUrgentClick)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Critical: $urgentVehicleName · $urgentReminderLabel",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB42318),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Resolve →",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleListRow(
    item: VehicleListItem,
    isHero: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicle = item.vehicle
    val distanceUnit = LocalDistanceUnit.current
    val title = VehicleDisplayNameFormatter.format(vehicle.make, vehicle.model, vehicle.year)
    val yearText = vehicle.year.takeIf { it > 0 }?.toString()

    val hasMileage = vehicle.currentOdometer > 0
    val formattedOdometer = remember(vehicle.currentOdometer, distanceUnit) {
        DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit))
    }
    val odometerText = if (hasMileage) {
        "$formattedOdometer ${DistanceUtil.unitLabel(distanceUnit)}"
    } else {
        stringResource(R.string.mileage_not_added)
    }

    val photoDescription = stringResource(R.string.cd_vehicle_photo_description, vehicle.make, vehicle.model)
    val statusLabel = stringResource(item.status.labelRes())
    val bodyType = remember(vehicle.make, vehicle.model) {
        com.autominder.app.domain.util.VehicleBodyTypeResolver.resolve(vehicle.make, vehicle.model)
    }

    // Predictive milestone teaser: "Next: Oil service in ~8,000 km (~Feb 2027)"
    val concernText = when {
        item.topConcern != null -> {
            val concernLabel = item.topConcern.customLabel?.takeIf { it.isNotBlank() }
                ?: item.topConcern.serviceType.localizedLabel()
            when (item.status) {
                ServiceStatus.OVERDUE -> stringResource(R.string.vehicle_list_concern_overdue, concernLabel)
                ServiceStatus.DUE_SOON -> stringResource(R.string.vehicle_list_concern_due_soon, concernLabel)
                else -> null
            }
        }
        item.nextServiceLabel != null -> {
            val distText = if (item.nextServiceRemainingKm != null && item.nextServiceRemainingKm > 0) {
                "in ~${DistanceFormat.grouped(item.nextServiceRemainingKm)} ${DistanceUtil.unitLabel(distanceUnit)}"
            } else null
            val dateText = item.nextServiceDueDate?.let { DateFormatUtil.formatDate(it) }

            val detail = listOfNotNull(distText, dateText).joinToString(" · ")
            if (detail.isNotBlank()) {
                "Next: ${item.nextServiceLabel} ($detail)"
            } else {
                "Next: ${item.nextServiceLabel}"
            }
        }
        item.status == ServiceStatus.UNKNOWN -> stringResource(R.string.vehicle_list_no_reminders)
        else -> null
    }

    val mergedDescription = listOfNotNull(title, item.roleLabel, yearText, odometerText, statusLabel, concernText)
        .joinToString(". ")

    VehicleHeroCard(
        title = title,
        modifier = modifier,
        variant = if (isHero) VehicleHeroVariant.Expanded else VehicleHeroVariant.Compact,
        yearText = yearText,
        roleBadgeText = item.roleLabel,
        odometerText = odometerText,
        photoUri = vehicle.photoUri,
        photoContentDescription = photoDescription,
        mergedContentDescription = mergedDescription,
        statusChip = { AutoMinderServiceStatusBadge(status = item.status) },
        railStatus = item.status,
        concernText = concernText,
        bodyType = bodyType,
        onClick = onClick
    )
}

@Composable
private fun ServiceStatus.labelRes(): Int = when (this) {
    ServiceStatus.OVERDUE -> R.string.status_overdue
    ServiceStatus.DUE_SOON -> R.string.status_due_soon
    ServiceStatus.SNOOZED -> R.string.status_snoozed
    ServiceStatus.OK -> R.string.state_all_clear
    ServiceStatus.COMPLETED -> R.string.status_completed
    ServiceStatus.UNKNOWN -> R.string.status_unknown
}
