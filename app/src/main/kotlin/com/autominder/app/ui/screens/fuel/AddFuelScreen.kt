package com.autominder.app.ui.screens.fuel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.BackHandler
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.autominder.app.R
import com.autominder.app.ui.components.DiscardChangesDialog
import com.autominder.app.ui.components.SaveButton
import com.autominder.app.ui.components.SaveButtonState
import com.autominder.app.ui.util.DateFormatUtil
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddFuelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val hasUnsavedChanges = uiState.volume.isNotBlank() ||
        uiState.cost.isNotBlank() ||
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

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fuel_add_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackRequest) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.volume,
                onValueChange = viewModel::onVolumeChanged,
                label = { Text(stringResource(R.string.fuel_label_volume, "L")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.cost,
                onValueChange = viewModel::onCostChanged,
                label = { Text(stringResource(R.string.fuel_label_cost, "$")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.odometer,
                onValueChange = viewModel::onOdometerChanged,
                label = { Text(stringResource(R.string.fuel_label_odometer, "km")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = DateFormatUtil.formatDate(uiState.date),
                onValueChange = {},
                label = { Text(stringResource(R.string.fuel_label_date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
                    }
                }
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChanged,
                label = { Text(stringResource(R.string.fuel_label_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SaveButton(
                state = when {
                    uiState.isSaved -> SaveButtonState.Success
                    uiState.isSaving -> SaveButtonState.Saving
                    else -> SaveButtonState.Idle
                },
                text = stringResource(R.string.fuel_action_save),
                onClick = viewModel::saveFuelEntry,
                modifier = Modifier.fillMaxWidth()
            )
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
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChanged(it) }
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
