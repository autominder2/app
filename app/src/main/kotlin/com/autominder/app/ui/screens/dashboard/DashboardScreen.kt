package com.autominder.app.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.VehicleOperationalStatus
import com.autominder.app.domain.usecase.PrioritizedReminder
import com.autominder.app.domain.usecase.ReminderExplanation
import com.autominder.app.domain.usecase.VehicleWithStatus
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.domain.util.VehicleDisplayNameFormatter
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.screens.dashboard.components.ActiveVehicleCard
import com.autominder.app.ui.screens.dashboard.components.ExplainableReminderSheet
import com.autominder.app.ui.screens.dashboard.components.QuickLogSection
import com.autominder.app.ui.screens.dashboard.components.RecentActivitySection
import com.autominder.app.ui.screens.dashboard.components.VehicleStatusCard
import com.autominder.app.ui.screens.dashboard.components.WhatsNextSection
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.util.DistanceFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToVehicleDetail: (Long) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToAddService: (Long) -> Unit,
    onNavigateToAddFuel: (Long) -> Unit,
    onNavigateToMileageLog: (Long) -> Unit,
    onNavigateToQuoteAuditor: (Long) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    var showVehicleSwitcherSheet by remember { mutableStateOf(false) }
    var selectedExplanation by remember { mutableStateOf<ReminderExplanation?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }
            is DashboardUiState.Empty -> {
                EmptyState(
                    title = stringResource(R.string.dashboard_no_vehicles_title),
                    subtitle = stringResource(R.string.dashboard_no_vehicles_subtitle),
                    actionLabel = stringResource(R.string.onboarding_add_my_car),
                    onAction = onNavigateToAddVehicle,
                    icon = Icons.Rounded.DirectionsCar
                )
            }
            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = stringResource(state.messageRes ?: R.string.dashboard_error),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            is DashboardUiState.Success -> {
                val activeVehicleWithStatus = state.selectedVehicle ?: state.vehicles.first()
                val activeVehicle = activeVehicleWithStatus.vehicle
                val distanceUnit = state.distanceUnit

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 16.dp,
                        // No FAB on this screen any more, and MainActivity's
                        // Scaffold already hands it innerPadding for the
                        // navigation bar — so this is breathing room only.
                        // It was 108.dp to clear a FAB that duplicated the
                        // inline Quick Log row; measured on a gesture-nav
                        // device, that left a 128.dp empty band below the
                        // last card.
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Top Brand Header + Notifications Bell
                    item(key = "home_brand_header") {
                        HomeTopBrandBar(
                            alertsCount = state.alertsCount,
                            onOpenNotifications = { /* Notifications center */ }
                        )
                    }

                    // 2. Contextual Greeting & State-driven Subhead
                    item(key = "home_greeting") {
                        HomeGreetingHeader(
                            greetingRes = DashboardViewModel.getGreetingRes(),
                            status = state.vehicleStatus,
                            upcomingCount = state.prioritizedReminders.size
                        )
                    }

                    // 3. Active Vehicle Card
                    item(key = "active_vehicle_${activeVehicle.id}") {
                        ActiveVehicleCard(
                            vehicle = activeVehicle,
                            totalVehiclesCount = state.vehicles.size,
                            distanceUnit = distanceUnit,
                            onNavigateToDetails = { onNavigateToVehicleDetail(activeVehicle.id) },
                            onOpenVehicleSwitcher = { showVehicleSwitcherSheet = true }
                        )
                    }

                    // 4. Vehicle Status Card (Reassuring Green Gradient Banner)
                    item(key = "vehicle_status_${state.vehicleStatus}") {
                        VehicleStatusCard(
                            status = state.vehicleStatus,
                            alertsCount = state.alertsCount,
                            nextCheck = state.nextCheck,
                            distanceUnit = distanceUnit,
                            onUpdateMileage = { onNavigateToVehicleDetail(activeVehicle.id) }
                        )
                    }

                    // 5. COMING UP Section
                    item(key = "whats_next_section") {
                        WhatsNextSection(
                            items = state.prioritizedReminders,
                            distanceUnit = distanceUnit,
                            onSeeAll = { onNavigateToVehicleDetail(activeVehicle.id) },
                            onExplainReminder = { item ->
                                selectedExplanation = viewModel.explainReminder(item)
                            }
                        )
                    }

                    // 6. QUICK LOG Section (3 balanced buttons)
                    item(key = "quick_log_section") {
                        QuickLogSection(
                            onLogService = { onNavigateToAddService(activeVehicle.id) },
                            onAddFuel = { onNavigateToAddFuel(activeVehicle.id) },
                            // Was onNavigateToVehicleDetail: this screen had no
                            // mileage callback at all, so a button labelled
                            // "Log mileage" opened the vehicle page instead.
                            // NavRoutes.MileageLog and MileageLogScreen already
                            // existed and VehicleDetail already reached them —
                            // only the dashboard was unwired.
                            onLogMileage = { onNavigateToMileageLog(activeVehicle.id) }
                        )
                    }

                    // 7. RECENT ACTIVITY Section
                    item(key = "recent_activity_section") {
                        RecentActivitySection(
                            items = state.recentActivity,
                            onSeeAll = { onNavigateToVehicleDetail(activeVehicle.id) },
                            onNavigateToItem = { /* Handled contextually */ }
                        )
                    }
                }


                // Deterministic Explainability Bottom Sheet
                selectedExplanation?.let { explanation ->
                    ExplainableReminderSheet(
                        explanation = explanation,
                        distanceUnit = distanceUnit,
                        onDismiss = { selectedExplanation = null },
                        onLogService = {
                            selectedExplanation = null
                            onNavigateToAddService(activeVehicle.id)
                        },
                        onEditReminder = {
                            selectedExplanation = null
                            onNavigateToVehicleDetail(activeVehicle.id)
                        }
                    )
                }


                // Multi-vehicle Switcher Bottom Sheet
                if (showVehicleSwitcherSheet && state.vehicles.size > 1) {
                    ModalBottomSheet(
                        onDismissRequest = { showVehicleSwitcherSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .navigationBarsPadding(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_pick_vehicle),
                                fontFamily = Exo2,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            state.vehicles.forEach { item ->
                                val isSelected = item.vehicle.id == activeVehicle.id
                                VehicleSwitcherRow(
                                    item = item,
                                    isSelected = isSelected,
                                    distanceUnit = distanceUnit,
                                    onClick = {
                                        viewModel.selectVehicle(item.vehicle.id)
                                        showVehicleSwitcherSheet = false
                                    }
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
private fun HomeTopBrandBar(
    alertsCount: Int,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "AutoMinder",
            fontFamily = Exo2,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = if (alertsCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeGreetingHeader(
    greetingRes: Int,
    status: VehicleOperationalStatus,
    upcomingCount: Int,
    modifier: Modifier = Modifier
) {
    val subtitle = when (status) {
        VehicleOperationalStatus.SETUP_INCOMPLETE -> "Finish setting up your car."
        VehicleOperationalStatus.OVERDUE -> "One item needs your attention."
        VehicleOperationalStatus.DUE_SOON -> "One service is coming up."
        VehicleOperationalStatus.UPCOMING -> "You have $upcomingCount things coming up."
        VehicleOperationalStatus.HEALTHY -> "Your car is on track."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stringResource(greetingRes),
            fontFamily = Exo2,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickLogSheetOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onClick()
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun VehicleSwitcherRow(
    item: VehicleWithStatus,
    isSelected: Boolean,
    distanceUnit: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val vehicle = item.vehicle
    val title = VehicleDisplayNameFormatter.format(vehicle.make, vehicle.model, vehicle.year)
    val displayOdo = DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit)
    val odoText = "${DistanceFormat.grouped(displayOdo)} ${DistanceUtil.unitLabel(distanceUnit)}"

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .semantics {
                role = Role.Button
                contentDescription = "$title, $odoText"
            },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = odoText,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
