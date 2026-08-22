package com.autominder.app.ui.screens.service

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.components.DiscardChangesDialog
import com.autominder.app.ui.components.SaveButton
import com.autominder.app.ui.components.SaveButtonState
import com.autominder.app.ui.components.ServiceChoicePicker
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel
import kotlinx.coroutines.delay

private val QUICK_COST_PRESETS = listOf("40", "75", "120", "250", "500")
private val ODOMETER_QUICK_ADJUSTMENTS = listOf(-500, 0, 500, 1000)

private data class IntervalPreset(val label: String, val months: Int, val kmDisplay: Int)
private val INTERVAL_PRESETS = listOf(
    IntervalPreset("3 mo / 5k", 3, 5000),
    IntervalPreset("6 mo / 10k", 6, 10000),
    IntervalPreset("12 mo / 20k", 12, 20000)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val hasUnsavedChanges = uiState.cost.isNotBlank() ||
        uiState.notes.isNotBlank() ||
        uiState.shopName.isNotBlank() ||
        uiState.customLabel.isNotBlank()

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
            keyboardController?.hide()
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.serviceDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(AddServiceUiEvent.ServiceDateChanged(datePickerState.selectedDateMillis))
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

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.add_service_title),
                            fontFamily = Exo2,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.vehicleName.isNotBlank()) {
                            val distanceUnit = LocalDistanceUnit.current
                            val odoFormatted = DistanceFormat.grouped(uiState.vehicleOdometerDisplay.toIntOrNull() ?: 0)
                            Text(
                                text = "${uiState.vehicleName} · $odoFormatted ${DistanceUtil.unitLabel(distanceUnit)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackRequest) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        AddServiceContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onOpenDatePicker = { showDatePicker = true },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun AddServiceContent(
    uiState: AddServiceUiState,
    onEvent: (AddServiceUiEvent) -> Unit,
    onOpenDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val distanceUnit = LocalDistanceUnit.current
    val unitLabel = DistanceUtil.unitLabel(distanceUnit)
    var showMoreWorkshop by remember { mutableStateOf(false) }
    var odometerFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: What was done? (Service Type Bento Picker)
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.add_service_what_was_done),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ServiceChoicePicker(
                        selected = uiState.serviceType,
                        recentTypes = uiState.recentTypes,
                        onSelected = { onEvent(AddServiceUiEvent.ServiceTypeChanged(it)) }
                    )

                    if (uiState.serviceType == ServiceType.CUSTOM) {
                        OutlinedTextField(
                            value = uiState.customLabel,
                            onValueChange = { onEvent(AddServiceUiEvent.CustomLabelChanged(it)) },
                            label = { Text(stringResource(R.string.add_service_name_label)) },
                            placeholder = { Text(stringResource(R.string.add_service_name_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true
                        )
                    }
                }
            }

            // Card 2: Date & Mileage Telemetry Card
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.telemetry_and_date_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1-Tap Date Presets
                    Text(
                        text = stringResource(R.string.add_service_date_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = isToday(uiState.serviceDate),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onEvent(AddServiceUiEvent.QuickDateSelected(0))
                            },
                            label = { Text(stringResource(R.string.quick_date_today)) },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = isYesterday(uiState.serviceDate),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onEvent(AddServiceUiEvent.QuickDateSelected(1))
                            },
                            label = { Text(stringResource(R.string.quick_date_yesterday)) },
                            shape = CircleShape
                        )
                        AssistChip(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onOpenDatePicker()
                            },
                            label = {
                                Text(
                                    text = uiState.serviceDate?.let { DateFormatUtil.formatDate(it) }
                                        ?: stringResource(R.string.quick_date_custom),
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = CircleShape
                        )
                    }

                    // Odometer at Service
                    OutlinedTextField(
                        value = if (odometerFocused) {
                            uiState.odometer
                        } else {
                            uiState.odometer.toIntOrNull()?.let { DistanceFormat.grouped(it) } ?: uiState.odometer
                        },
                        onValueChange = { onEvent(AddServiceUiEvent.OdometerChanged(it)) },
                        label = { Text(stringResource(R.string.add_service_odometer_label, unitLabel)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { odometerFocused = it.isFocused },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )

                    // 1-Tap Odometer Delta Adjustments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick adjust:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ODOMETER_QUICK_ADJUSTMENTS.forEach { delta ->
                            AssistChip(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    if (delta == 0) {
                                        onEvent(AddServiceUiEvent.OdometerChanged(uiState.vehicleOdometerDisplay))
                                    } else {
                                        onEvent(AddServiceUiEvent.OdometerAdjusted(delta))
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (delta == 0) "Current" else (if (delta > 0) "+$delta" else "$delta"),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono)
                                    )
                                },
                                shape = CircleShape,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        }
                    }
                }
            }

            // Card 3: Next Service Auto-Reminder Smart Predictor
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.add_service_remind_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.add_service_remind_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.remindNext,
                            onCheckedChange = {
                                haptic.performHapticFeedback(
                                    if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                                )
                                onEvent(AddServiceUiEvent.RemindNextToggled(it))
                            }
                        )
                    }

                    AnimatedVisibility(visible = uiState.remindNext) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.animateContentSize()
                        ) {
                            // Quick Interval Presets
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(INTERVAL_PRESETS) { preset ->
                                    val isSelected = uiState.remindIntervalMonths == preset.months.toString()
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            onEvent(
                                                AddServiceUiEvent.QuickIntervalPresetSelected(
                                                    months = preset.months,
                                                    kmDisplay = preset.kmDisplay
                                                )
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = preset.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        shape = CircleShape
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.remindIntervalKm,
                                    onValueChange = { onEvent(AddServiceUiEvent.RemindKmChanged(it)) },
                                    label = { Text(stringResource(R.string.add_service_remind_every_distance, unitLabel)) },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = uiState.remindIntervalMonths,
                                    onValueChange = { onEvent(AddServiceUiEvent.RemindMonthsChanged(it)) },
                                    label = { Text(stringResource(R.string.add_service_remind_every_months)) },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }

                            // Live Forecast Preview Pill
                            if (uiState.predictedNextDueDateFormatted.isNotBlank() || uiState.predictedNextDueOdometerDisplay.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        val typeLabel = if (uiState.serviceType == ServiceType.CUSTOM && uiState.customLabel.isNotBlank()) {
                                            uiState.customLabel
                                        } else {
                                            uiState.serviceType.localizedLabel()
                                        }
                                        Text(
                                            text = stringResource(
                                                R.string.next_service_prediction_text,
                                                typeLabel,
                                                uiState.predictedNextDueDateFormatted,
                                                uiState.predictedNextDueOdometerDisplay,
                                                unitLabel
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 4: Cost, Workshop & Notes Bento Card
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cost_and_shop_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Quick Cost Presets
                    Text(
                        text = stringResource(R.string.quick_cost_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(QUICK_COST_PRESETS) { preset ->
                            val isSelected = uiState.cost == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onEvent(AddServiceUiEvent.QuickCostSelected(preset))
                                },
                                label = {
                                    Text(
                                        text = "$$preset",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = CircleShape
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.cost,
                        onValueChange = { onEvent(AddServiceUiEvent.CostChanged(it)) },
                        label = { Text(stringResource(R.string.add_service_cost_label)) },
                        placeholder = { Text(stringResource(R.string.add_service_cost_placeholder)) },
                        prefix = { Text(stringResource(R.string.add_service_cost_prefix)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    // Collapsible Workshop & Notes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showMoreWorkshop = !showMoreWorkshop }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showMoreWorkshop) "Hide workshop & notes" else "Add workshop & notes",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (showMoreWorkshop) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = showMoreWorkshop) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = uiState.shopName,
                                onValueChange = { onEvent(AddServiceUiEvent.ShopNameChanged(it)) },
                                label = { Text(stringResource(R.string.add_service_shop_label)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Store,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.notes,
                                onValueChange = { onEvent(AddServiceUiEvent.NotesChanged(it)) },
                                label = { Text(stringResource(R.string.add_service_notes_label)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Notes,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                minLines = 3
                            )
                        }
                    }
                }
            }

            uiState.errorRes?.let { errorRes ->
                Text(
                    text = stringResource(errorRes, *uiState.errorArgs.toTypedArray()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sticky Bottom Save Button Action Dock
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                SaveButton(
                    state = when {
                        uiState.isSaved -> SaveButtonState.Success
                        uiState.isLoading -> SaveButtonState.Saving
                        else -> SaveButtonState.Idle
                    },
                    text = stringResource(R.string.add_service_save),
                    onClick = { onEvent(AddServiceUiEvent.SaveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }
    }
}

private fun isToday(timeMillis: Long?): Boolean {
    if (timeMillis == null) return false
    val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }
    val cal2 = java.util.Calendar.getInstance()
    return cal1.get(java.util.Calendar.ERA) == cal2.get(java.util.Calendar.ERA) &&
        cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
        cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}

private fun isYesterday(timeMillis: Long?): Boolean {
    if (timeMillis == null) return false
    val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }
    val cal2 = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
    return cal1.get(java.util.Calendar.ERA) == cal2.get(java.util.Calendar.ERA) &&
        cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
        cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}
