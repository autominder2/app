package com.autominder.app.ui.screens.service

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.components.DiscardChangesDialog
import com.autominder.app.ui.components.FormField
import com.autominder.app.ui.components.SaveButton
import com.autominder.app.ui.components.SaveButtonState
import com.autominder.app.ui.components.ServiceChoicePicker
import com.autominder.app.ui.util.DistanceFormat
import kotlinx.coroutines.delay
import com.autominder.app.ui.util.DateFormatUtil

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
    var showMore by remember { mutableStateOf(false) }
    var odometerFocused by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Pre-filled defaults (date, odometer) don't count as edits
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
            // The save button morphs to its Success state — let it register,
            // then leave. The button itself is the confirmation.
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
        val datePickerState = rememberDatePickerState()
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
        // One owner for IME insets: lifting the whole Scaffold keeps the sticky
        // Save above the keyboard without double-padding the scrolling content.
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.add_service_title))
                        // Quiet context — which car this is being logged against.
                        // No hero, no photograph; the vehicle is a fact, not a banner.
                        if (uiState.vehicleName.isNotBlank()) {
                            Text(
                                text = stringResource(
                                    R.string.add_service_vehicle_context,
                                    uiState.vehicleName,
                                    "${DistanceFormat.grouped(uiState.vehicleOdometerDisplay.toIntOrNull() ?: 0)} ${DistanceUtil.unitLabel(LocalDistanceUnit.current)}"
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackRequest) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        bottomBar = {
            // The screen's job is Save. It must never scroll out of reach behind
            // optional details or a long service list.
            Surface(color = MaterialTheme.colorScheme.surface) {
                SaveButton(
                    state = when {
                        uiState.isSaved -> SaveButtonState.Success
                        uiState.isLoading -> SaveButtonState.Saving
                        else -> SaveButtonState.Idle
                    },
                    text = stringResource(R.string.add_service_save),
                    onClick = { viewModel.onEvent(AddServiceUiEvent.SaveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            FormField(index = 0) {
                Column {
                    Text(
                        text = stringResource(R.string.add_service_what_was_done),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ServiceChoicePicker(
                        selected = uiState.serviceType,
                        recentTypes = uiState.recentTypes,
                        onSelected = { viewModel.onEvent(AddServiceUiEvent.ServiceTypeChanged(it)) }
                    )
                }
            }

            if (uiState.serviceType == ServiceType.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.customLabel,
                    onValueChange = { viewModel.onEvent(AddServiceUiEvent.CustomLabelChanged(it)) },
                    label = { Text(stringResource(R.string.add_service_name_label)) },
                    placeholder = { Text(stringResource(R.string.add_service_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormField(index = 1) {
                // Grouped while resting so the number reads like an odometer;
                // raw while editing so typing and the cursor behave normally.
                OutlinedTextField(
                    value = if (odometerFocused) {
                        uiState.odometer
                    } else {
                        uiState.odometer.toIntOrNull()?.let { DistanceFormat.grouped(it) }
                            ?: uiState.odometer
                    },
                    onValueChange = { viewModel.onEvent(AddServiceUiEvent.OdometerChanged(it)) },
                    label = { Text(stringResource(R.string.add_service_odometer_label, DistanceUtil.unitLabel(LocalDistanceUnit.current))) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { odometerFocused = it.isFocused },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormField(index = 2) {
                OutlinedTextField(
                    value = uiState.serviceDate?.let {
                        DateFormatUtil.formatDate(it)
                    } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.add_service_date_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.add_service_select_date))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Headline: the log-and-never-forget prompt
            FormField(index = 3) {
                ReminderPromptCard(
                    enabled = uiState.remindNext,
                    intervalDistance = uiState.remindIntervalKm,
                    intervalMonths = uiState.remindIntervalMonths,
                    unitLabel = DistanceUtil.unitLabel(LocalDistanceUnit.current),
                    onToggle = { viewModel.onEvent(AddServiceUiEvent.RemindNextToggled(it)) },
                    onDistanceChanged = { viewModel.onEvent(AddServiceUiEvent.RemindKmChanged(it)) },
                    onMonthsChanged = { viewModel.onEvent(AddServiceUiEvent.RemindMonthsChanged(it)) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Optional details fold away so the default view stays short
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showMore = !showMore }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_vehicle_more_details),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (showMore) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = showMore) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.cost,
                        onValueChange = { viewModel.onEvent(AddServiceUiEvent.CostChanged(it)) },
                        label = { Text(stringResource(R.string.add_service_cost_label)) },
                        placeholder = { Text(stringResource(R.string.add_service_cost_placeholder)) },
                        prefix = { Text(stringResource(R.string.add_service_cost_prefix)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.shopName,
                        onValueChange = { viewModel.onEvent(AddServiceUiEvent.ShopNameChanged(it)) },
                        label = { Text(stringResource(R.string.add_service_shop_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onEvent(AddServiceUiEvent.NotesChanged(it)) },
                        label = { Text(stringResource(R.string.add_service_notes_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            uiState.errorRes?.let { errorRes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(errorRes, *uiState.errorArgs.toTypedArray()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * "Remind me for the next one" — turns a one-off log into an ongoing schedule.
 * Pre-filled with a sensible interval for the chosen service type, editable,
 * and dismissable. This is what makes users never have to remember a service.
 */
@Composable
private fun ReminderPromptCard(
    enabled: Boolean,
    intervalDistance: String,
    intervalMonths: String,
    unitLabel: String,
    onToggle: (Boolean) -> Unit,
    onDistanceChanged: (String) -> Unit,
    onMonthsChanged: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f))
            .padding(16.dp)
    ) {
        // Top-aligned: at large text sizes the subtitle wraps to several lines and
        // centre alignment would drag the icon and switch into the middle of it.
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    // Explicit gutter so wrapped lines can never run under the
                    // trailing switch, at any text scale.
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_service_remind_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.add_service_remind_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    haptic.performHapticFeedback(
                        if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                    )
                    onToggle(it)
                }
            )
        }
        AnimatedVisibility(visible = enabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = intervalDistance,
                    onValueChange = onDistanceChanged,
                    label = { Text(stringResource(R.string.add_service_remind_every_distance, unitLabel)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = intervalMonths,
                    onValueChange = onMonthsChanged,
                    label = { Text(stringResource(R.string.add_service_remind_every_months)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}
