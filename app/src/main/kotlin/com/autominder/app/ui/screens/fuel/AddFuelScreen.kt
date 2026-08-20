package com.autominder.app.ui.screens.fuel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.components.DiscardChangesDialog
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.ListSkeleton
import com.autominder.app.ui.components.SaveButton
import com.autominder.app.ui.components.SaveButtonState
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddFuelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.volume.isNotBlank() ||
        uiState.cost.isNotBlank() ||
        uiState.pricePerUnit.isNotBlank() ||
        uiState.gasStation.isNotBlank() ||
        uiState.notes.isNotBlank()

    val onBackRequest: () -> Unit = {
        if (hasUnsavedChanges && !uiState.isSaved) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = hasUnsavedChanges && !uiState.isSaved) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDiscard = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onKeepEditing = { showDiscardDialog = false }
        )
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            delay(650)
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorRes) {
        if (uiState.errorRes != null) {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.fuel_add_title),
                        fontFamily = Exo2,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onBackRequest()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    SaveButton(
                        state = when {
                            uiState.isSaved -> SaveButtonState.Success
                            uiState.isSaving -> SaveButtonState.Saving
                            else -> SaveButtonState.Idle
                        },
                        text = stringResource(R.string.fuel_action_save),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            viewModel.onEvent(AddFuelUiEvent.SaveClicked)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> ListSkeleton(rows = 5)
                uiState.errorRes != null && !uiState.isSaving -> ErrorState(
                    message = stringResource(uiState.errorRes!!, *uiState.errorArgs.toTypedArray()),
                    onRetry = { viewModel.onEvent(AddFuelUiEvent.RetryClicked) }
                )
                else -> AddFuelBentoContent(
                    uiState = uiState,
                    showMoreOptions = showMoreOptions,
                    onToggleMoreOptions = { showMoreOptions = !showMoreOptions },
                    onDatePickerClick = { showDatePicker = true },
                    onEvent = viewModel::onEvent
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(AddFuelUiEvent.DateChanged(it))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 2026 Material 3 Expressive Bento Grid Content for Add Fuel
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddFuelBentoContent(
    uiState: AddFuelUiState,
    showMoreOptions: Boolean,
    onToggleMoreOptions: () -> Unit,
    onDatePickerClick: () -> Unit,
    onEvent: (AddFuelUiEvent) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val distanceUnit = LocalDistanceUnit.current
    val unitLabel = DistanceUtil.unitLabel(distanceUnit)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Vehicle Context Digital Twin Card
        uiState.vehicle?.let { vehicle ->
            item(key = "vehicle_context") {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.fuel_add_title).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = Exo2,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "${DistanceFormat.grouped(DistanceUtil.kmToDisplay(vehicle.currentOdometer, distanceUnit))} $unitLabel",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Bento Card 1: Fuel Volume, Total Cost & Smart Math Solver
        item(key = "fuel_math_card") {
            ElevatedCard(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header & Full Tank Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.paywall_feature_fuel_log),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = Exo2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Full tank switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.fuel_full_tank_toggle),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = uiState.isFullTank,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onEvent(AddFuelUiEvent.FullTankToggled(it))
                                }
                            )
                        }
                    }

                    // Total Cost Field
                    OutlinedTextField(
                        value = uiState.cost,
                        onValueChange = { onEvent(AddFuelUiEvent.CostChanged(it)) },
                        label = { Text(stringResource(R.string.fuel_label_cost, "$")) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Tap-Don't-Type Quick Cost Presets
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.fuel_quick_cost_title).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(20.0, 30.0, 50.0, 75.0, 100.0).forEach { presetCost ->
                                val label = "$${presetCost.toInt()}"
                                FilterChip(
                                    selected = uiState.cost == presetCost.toInt().toString(),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        onEvent(AddFuelUiEvent.QuickCostTapped(presetCost))
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Volume Field
                    OutlinedTextField(
                        value = uiState.volume,
                        onValueChange = { onEvent(AddFuelUiEvent.VolumeChanged(it)) },
                        label = { Text(stringResource(R.string.fuel_label_volume, "L")) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Tap-Don't-Type Quick Volume Presets
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.fuel_quick_volume_title).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(10.0, 20.0, 35.0, 50.0, 60.0).forEach { presetVol ->
                                val label = "${presetVol.toInt()} L"
                                FilterChip(
                                    selected = uiState.volume == presetVol.toInt().toString(),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        onEvent(AddFuelUiEvent.QuickVolumeTapped(presetVol))
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Optional Price per unit pill for smart calculation
                    OutlinedTextField(
                        value = uiState.pricePerUnit,
                        onValueChange = { onEvent(AddFuelUiEvent.PricePerUnitChanged(it)) },
                        label = { Text(stringResource(R.string.fuel_price_per_unit_label, "L")) },
                        placeholder = { Text(stringResource(R.string.fuel_price_per_unit_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        // 3. Bento Card 2: Odometer & Smart Trip Steppers
        item(key = "odometer_card") {
            ElevatedCard(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.fuel_label_odometer, unitLabel),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = Exo2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Driven since last fill-up badge
                        uiState.distanceSinceLastFill?.let { delta ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "+$delta $unitLabel",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.odometer,
                        onValueChange = { onEvent(AddFuelUiEvent.OdometerChanged(it)) },
                        label = { Text(stringResource(R.string.label_current_odometer_dynamic, unitLabel)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )

                    // Quick Step Delta Pills
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(50, 100, 250, 500).forEach { step ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onEvent(AddFuelUiEvent.QuickOdometerStepTapped(step))
                                },
                                label = {
                                    Text(
                                        text = "+$step $unitLabel",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Bento Card 3: Live Telemetry & Economy Preview (Instant Gratification)
        if (uiState.estimatedEfficiency != null || uiState.distanceSinceLastFill != null) {
            item(key = "live_telemetry_card") {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.fuel_live_telemetry_title).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uiState.estimatedEfficiency?.let { eff ->
                                FuelStatPill(
                                    title = stringResource(R.string.fuel_efficiency_label),
                                    value = stringResource(R.string.fuel_live_efficiency_stat, eff, "$unitLabel/L"),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            uiState.estimatedCostPerDistance?.let { costPerKm ->
                                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                                FuelStatPill(
                                    title = stringResource(R.string.vehicle_detail_mileage),
                                    value = stringResource(R.string.fuel_live_cost_per_km, currencyFormat.format(costPerKm), unitLabel),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Bento Card 4: Collapsible Gas Station, Date & Notes
        item(key = "collapsible_notes") {
            ElevatedCard(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onToggleMoreOptions()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.add_vehicle_more_details),
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = Exo2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showMoreOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(
                        visible = showMoreOptions,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Date field
                            OutlinedTextField(
                                value = DateFormatUtil.formatDate(uiState.date),
                                onValueChange = {},
                                label = { Text(stringResource(R.string.fuel_label_date)) },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                shape = MaterialTheme.shapes.medium,
                                trailingIcon = {
                                    IconButton(onClick = onDatePickerClick) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = stringResource(R.string.cd_select_date),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )

                            // Gas Station / Brand
                            OutlinedTextField(
                                value = uiState.gasStation,
                                onValueChange = { onEvent(AddFuelUiEvent.GasStationChanged(it)) },
                                label = { Text(stringResource(R.string.fuel_station_label)) },
                                placeholder = { Text(stringResource(R.string.fuel_station_placeholder)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium
                            )

                            // Notes
                            OutlinedTextField(
                                value = uiState.notes,
                                onValueChange = { onEvent(AddFuelUiEvent.NotesChanged(it)) },
                                label = { Text(stringResource(R.string.fuel_label_notes)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Notes,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = MaterialTheme.shapes.medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fuel Stat Mini-Pill
 */
@Composable
private fun FuelStatPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
