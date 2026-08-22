package com.autominder.app.ui.screens.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                        text = stringResource(R.string.vehicle_list_title),
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
                    title = stringResource(R.string.vehicle_list_empty_title),
                    subtitle = stringResource(R.string.vehicle_list_empty_subtitle),
                    onAction = onNavigateToAddVehicle,
                    actionLabel = stringResource(R.string.action_add_vehicle)
                )
                is VehicleListUiState.Error -> ErrorState(
                    message = stringResource(state.messageRes),
                    onRetry = { viewModel.retry() }
                )
                is VehicleListUiState.Success -> VehicleListContent(
                    items = state.items,
                    attentionCount = state.attentionCount,
                    onVehicleClick = onNavigateToVehicleDetail
                )
            }
        }
    }
}

@Composable
private fun VehicleListContent(
    items: List<VehicleListItem>,
    attentionCount: Int,
    onVehicleClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "verdict") {
            VerdictSentence(attentionCount = attentionCount)
        }
        itemsIndexed(items, key = { _, item -> item.vehicle.id }) { index, item ->
            VehicleListRow(
                item = item,
                isHero = index == 0,
                onClick = { onVehicleClick(item.vehicle.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

/** The single plain-language verdict for this screen — reuses the exact
 *  same aggregate copy Dashboard uses for the identical underlying
 *  overdue+due-soon count, so the two screens never disagree in tone. */
@Composable
private fun VerdictSentence(attentionCount: Int, modifier: Modifier = Modifier) {
    val text = if (attentionCount == 0) {
        stringResource(R.string.dashboard_all_clear_supporting)
    } else {
        pluralStringResource(R.plurals.dashboard_attention_headline, attentionCount, attentionCount)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = if (attentionCount == 0) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    )
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
    // A blank odometer field is stored as 0 (AddVehicleViewModel:
    // `toIntOrNull() ?: 0`), so 0 means "never entered" in practice — the
    // column is non-null Int and cannot distinguish it from a typed 0.
    // Showing "0 km" as if it were a real reading is the dishonest option;
    // showing "Mileage not added" is right in the overwhelmingly common
    // case and is trivially corrected by the user. A nullable column would
    // make this exact, but that is a schema migration and its own branch.
    val hasMileage = vehicle.currentOdometer > 0
    val formattedOdometer = remember(vehicle.currentOdometer, distanceUnit) {
        DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit))
    }
    val odometerText = if (hasMileage) {
        "$formattedOdometer ${DistanceUtil.unitLabel(distanceUnit)}"
    } else {
        stringResource(R.string.vehicle_list_mileage_not_added)
    }
    val photoDescription = stringResource(R.string.cd_vehicle_photo_description, vehicle.make, vehicle.model)
    val statusLabel = stringResource(item.status.labelRes())

    val concernText = when {
        // Truthful no-data state: absent reminders are not "all good".
        item.status == ServiceStatus.UNKNOWN ->
            stringResource(R.string.vehicle_list_no_reminders)
        else -> item.topConcern?.let { reminder ->
            val concernLabel = reminder.customLabel?.takeIf { it.isNotBlank() }
                ?: reminder.serviceType.localizedLabel()
            when (item.status) {
                ServiceStatus.OVERDUE -> stringResource(R.string.vehicle_list_concern_overdue, concernLabel)
                ServiceStatus.DUE_SOON -> stringResource(R.string.vehicle_list_concern_due_soon, concernLabel)
                else -> null
            }
        }
    }

    // Each row announces as one coherent unit for TalkBack (a list of many
    // cards benefits from one swipe per vehicle, unlike the single Detail
    // hero which stays unmerged for granular exploration).
    val mergedDescription = listOfNotNull(title, yearText, odometerText, statusLabel, concernText)
        .joinToString(". ")

    VehicleHeroCard(
        title = title,
        modifier = modifier,
        variant = if (isHero) VehicleHeroVariant.Expanded else VehicleHeroVariant.Compact,
        yearText = yearText,
        odometerText = odometerText,
        photoUri = vehicle.photoUri,
        photoContentDescription = photoDescription,
        mergedContentDescription = mergedDescription,
        statusChip = { AutoMinderServiceStatusBadge(status = item.status) },
        railStatus = item.status,
        concernText = concernText,
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
