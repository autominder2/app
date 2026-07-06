package com.autominder.app.ui.screens.reminder

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.localizedLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Snapshot the pre-populated form once loaded; dirty = any field differs
    var initialSnapshot by remember { mutableStateOf<EditReminderUiState?>(null) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && initialSnapshot == null) {
            initialSnapshot = uiState
        }
    }
    val hasUnsavedChanges = initialSnapshot?.let { s ->
        s.serviceType != uiState.serviceType ||
            s.customLabel != uiState.customLabel ||
            s.dueKm != uiState.dueKm ||
            s.dueDateLong != uiState.dueDateLong ||
            s.intervalKm != uiState.intervalKm ||
            s.intervalDays != uiState.intervalDays ||
            s.notes != uiState.notes
    } ?: false

    val onBackRequest: () -> Unit = {
        if (hasUnsavedChanges && !uiState.isSaved && !uiState.isDeleted) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    androidx.activity.compose.BackHandler(enabled = hasUnsavedChanges && !uiState.isSaved && !uiState.isDeleted) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        com.autominder.app.ui.components.DiscardChangesDialog(
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
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.Confirm)
            kotlinx.coroutines.delay(650)
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            keyboardController?.hide()
            onNavigateBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.edit_reminder_delete_title)) },
            text = { Text(stringResource(R.string.edit_reminder_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.onEvent(EditReminderUiEvent.DeleteClicked)
                }) {
                    Text(stringResource(R.string.service_detail_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDateLong
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(EditReminderUiEvent.DueDateChanged(datePickerState.selectedDateMillis))
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
                title = { Text(stringResource(R.string.edit_reminder_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackRequest) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.edit_reminder_delete))
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
            Text(
                text = stringResource(R.string.label_service_type),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.serviceType.localizedLabel(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    ServiceType.entries.forEach { serviceType ->
                        DropdownMenuItem(
                            text = { Text(serviceType.localizedLabel()) },
                            onClick = {
                                viewModel.onEvent(EditReminderUiEvent.ServiceTypeChanged(serviceType))
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.serviceType == ServiceType.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.customLabel,
                    onValueChange = { viewModel.onEvent(EditReminderUiEvent.CustomLabelChanged(it)) },
                    label = { Text(stringResource(R.string.label_reminder_name)) },
                    placeholder = { Text(stringResource(R.string.label_reminder_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.dueKm,
                onValueChange = { viewModel.onEvent(EditReminderUiEvent.DueKmChanged(it)) },
                label = { Text(stringResource(R.string.label_due_at_odometer_km)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.dueDateLong?.let {
                    DateFormatUtil.formatDate(it)
                } ?: "",
                onValueChange = {},
                label = { Text(stringResource(R.string.label_due_date_optional)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.label_select_date))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.intervalKm,
                onValueChange = { viewModel.onEvent(EditReminderUiEvent.IntervalKmChanged(it)) },
                label = { Text(stringResource(R.string.label_interval_km)) },
                placeholder = { Text(stringResource(R.string.label_interval_km_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.intervalDays,
                onValueChange = { viewModel.onEvent(EditReminderUiEvent.IntervalDaysChanged(it)) },
                label = { Text(stringResource(R.string.label_interval_days)) },
                placeholder = { Text(stringResource(R.string.label_interval_days_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onEvent(EditReminderUiEvent.NotesChanged(it)) },
                label = { Text(stringResource(R.string.label_notes_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            uiState.errorRes?.let { errorRes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(errorRes, *uiState.errorArgs.toTypedArray()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onEvent(EditReminderUiEvent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) stringResource(R.string.action_saving) else stringResource(R.string.action_save_changes))
            }
        }
    }
}
