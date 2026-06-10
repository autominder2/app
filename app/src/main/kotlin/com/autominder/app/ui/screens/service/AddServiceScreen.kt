package com.autominder.app.ui.screens.service

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
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
import com.autominder.app.ui.components.ServiceTypeGrid
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

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_service_title)) },
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
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            FormField(index = 0) {
                Column {
                    Text(
                        text = stringResource(R.string.add_service_type_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ServiceTypeGrid(
                        selected = uiState.serviceType,
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
                OutlinedTextField(
                    value = uiState.odometer,
                    onValueChange = { viewModel.onEvent(AddServiceUiEvent.OdometerChanged(it)) },
                    label = { Text("Odometer at Service (${DistanceUtil.unitLabel(LocalDistanceUnit.current)})") },
                    modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(16.dp))

            FormField(index = 3) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormField(index = 4) {
                OutlinedTextField(
                    value = uiState.shopName,
                    onValueChange = { viewModel.onEvent(AddServiceUiEvent.ShopNameChanged(it)) },
                    label = { Text(stringResource(R.string.add_service_shop_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormField(index = 5) {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onEvent(AddServiceUiEvent.NotesChanged(it)) },
                    label = { Text(stringResource(R.string.add_service_notes_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SaveButton(
                state = when {
                    uiState.isSaved -> SaveButtonState.Success
                    uiState.isLoading -> SaveButtonState.Saving
                    else -> SaveButtonState.Idle
                },
                text = stringResource(R.string.add_service_save),
                onClick = { viewModel.onEvent(AddServiceUiEvent.SaveClicked) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
