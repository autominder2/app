package com.autominder.app.ui.screens.vehicle

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.autominder.app.R
import com.autominder.app.domain.usecase.PlannedReminder
import com.autominder.app.ui.components.DiscardChangesDialog
import com.autominder.app.ui.components.LocalSnackbarHostState
import com.autominder.app.ui.components.SaveButton
import com.autominder.app.ui.components.SaveButtonState
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import java.text.NumberFormat

private val QUICK_YEARS = listOf("2026", "2025", "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2015", "2010", "2005")
private val GARAGE_ROLES = listOf("Daily Driver", "Family", "Weekend", "Work")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddVehicleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = LocalSnackbarHostState.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.brand.isNotBlank() ||
        uiState.model.isNotBlank() ||
        uiState.year.isNotBlank() ||
        uiState.currentOdometer.isNotBlank() ||
        uiState.photoUri != null

    val onBackRequest: () -> Unit = {
        if (uiState.currentStep != AddVehicleStep.DISCOVERY) {
            viewModel.onEvent(AddVehicleUiEvent.PreviousStepClicked)
        } else if (hasUnsavedChanges && !uiState.isSaved) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = true) {
        onBackRequest()
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Ignore if persistable permission not supported
            }
            viewModel.onEvent(AddVehicleUiEvent.PhotoUriChanged(it.toString()))
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            keyboardController?.hide()
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.add_vehicle_celebration_sub,
                    uiState.brand,
                    uiState.model
                ),
                duration = SnackbarDuration.Short
            )
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.add_vehicle_title),
                            fontFamily = Exo2,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackRequest,
                            modifier = Modifier.size(48.dp)
                        ) {
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
                // 3-Step Stepper Progress Bar
                StepperHeader(currentStep = uiState.currentStep)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "stepTransition"
            ) { step ->
                when (step) {
                    AddVehicleStep.DISCOVERY -> {
                        UniversalDiscoveryStepContent(
                            uiState = uiState,
                            onEvent = viewModel::onEvent
                        )
                    }
                    AddVehicleStep.IDENTITY -> {
                        IdentityStepContent(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onPickPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                    AddVehicleStep.SCHEDULE -> {
                        ScheduleStepContent(
                            uiState = uiState,
                            onEvent = viewModel::onEvent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperHeader(currentStep: AddVehicleStep) {
    val progress by animateFloatAsState(
        targetValue = when (currentStep) {
            AddVehicleStep.DISCOVERY -> 0.33f
            AddVehicleStep.IDENTITY -> 0.66f
            AddVehicleStep.SCHEDULE -> 1.0f
        },
        label = "stepperProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepSegmentText(
                title = stringResource(R.string.add_vehicle_step_1),
                isActive = currentStep == AddVehicleStep.DISCOVERY,
                isCompleted = currentStep.ordinal > AddVehicleStep.DISCOVERY.ordinal
            )
            StepSegmentText(
                title = stringResource(R.string.add_vehicle_step_2),
                isActive = currentStep == AddVehicleStep.IDENTITY,
                isCompleted = currentStep.ordinal > AddVehicleStep.IDENTITY.ordinal
            )
            StepSegmentText(
                title = stringResource(R.string.add_vehicle_step_3),
                isActive = currentStep == AddVehicleStep.SCHEDULE,
                isCompleted = false
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun StepSegmentText(
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val textColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primary
            isCompleted -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        label = "stepColor"
    )

    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
        color = textColor
    )
}

/**
 * Step 1: Universal Search-First Discovery
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UniversalDiscoveryStepContent(
    uiState: AddVehicleUiState,
    onEvent: (AddVehicleUiEvent) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val canProceed = uiState.brand.isNotBlank() && uiState.model.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Intro
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.add_vehicle_headline),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.add_vehicle_subhead),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Universal Search Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(AddVehicleUiEvent.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.add_vehicle_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onEvent(AddVehicleUiEvent.SearchQueryChanged("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Vehicle Suggestions Deck
            Text(
                text = stringResource(R.string.add_vehicle_popular_makes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.suggestions.forEach { suggestion ->
                    val isSelected = uiState.brand.equals(suggestion.make, ignoreCase = true) &&
                        uiState.model.equals(suggestion.model, ignoreCase = true)

                    OutlinedCard(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEvent(AddVehicleUiEvent.SuggestionSelected(suggestion))
                        },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.semantics {
                            contentDescription = "Select ${suggestion.make} ${suggestion.model} vehicle"
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${suggestion.make} ${suggestion.model}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (suggestion.badge != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = suggestion.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Custom Vehicle Fallback
            OutlinedButton(
                onClick = { onEvent(AddVehicleUiEvent.CustomEntryToggled(!uiState.isCustomEntry)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_vehicle_custom_option))
            }

            // Manual Entry Fallback Fields
            AnimatedVisibility(visible = uiState.isCustomEntry) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.brand,
                        onValueChange = { onEvent(AddVehicleUiEvent.BrandChanged(it)) },
                        label = { Text("Make / Brand (e.g. Toyota)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.model,
                        onValueChange = { onEvent(AddVehicleUiEvent.ModelChanged(it)) },
                        label = { Text("Model (e.g. RAV4)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (uiState.errorRes != null) {
                Text(
                    text = stringResource(uiState.errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Bottom CTA
        Button(
            onClick = { onEvent(AddVehicleUiEvent.NextStepClicked) },
            enabled = canProceed,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = stringResource(R.string.add_vehicle_next_step),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Step 2: Vehicle Identity & Persona
 */
@Composable
private fun IdentityStepContent(
    uiState: AddVehicleUiState,
    onEvent: (AddVehicleUiEvent) -> Unit,
    onPickPhoto: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected Vehicle Header Card
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${uiState.brand} ${uiState.model}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vehicle Identity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onEvent(AddVehicleUiEvent.PreviousStepClicked) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit vehicle make and model",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Optional Photo Card
            VehiclePhotoHero(
                photoUri = uiState.photoUri,
                onPickPhoto = onPickPhoto
            )

            // Year Selector Section
            Text(
                text = stringResource(R.string.add_vehicle_year_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(QUICK_YEARS) { year ->
                    val isSelected = uiState.year == year
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEvent(AddVehicleUiEvent.YearChanged(year))
                        },
                        label = {
                            Text(
                                text = year,
                                fontFamily = JetBrainsMono,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Garage Role Prompt (Optional)
            Text(
                text = stringResource(R.string.add_vehicle_role_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GARAGE_ROLES.forEach { role ->
                    val isSelected = uiState.role == role
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEvent(AddVehicleUiEvent.RoleChanged(role))
                        },
                        label = {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.errorRes != null) {
                Text(
                    text = stringResource(uiState.errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onEvent(AddVehicleUiEvent.PreviousStepClicked) },
                modifier = Modifier
                    .weight(0.35f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back")
            }
            Button(
                onClick = { onEvent(AddVehicleUiEvent.NextStepClicked) },
                modifier = Modifier
                    .weight(0.65f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_vehicle_next_step),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Step 3: Odometer & Confidence-First Maintenance Plan
 */
@Composable
private fun ScheduleStepContent(
    uiState: AddVehicleUiState,
    onEvent: (AddVehicleUiEvent) -> Unit
) {
    val distanceUnit = LocalDistanceUnit.current
    val haptic = LocalHapticFeedback.current
    var showAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Odometer Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.add_vehicle_odometer_headline),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.add_vehicle_odometer_subhead),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Odometer Input Field
            OutlinedTextField(
                value = uiState.currentOdometer,
                onValueChange = { onEvent(AddVehicleUiEvent.OdometerChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0", fontFamily = JetBrainsMono) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                suffix = {
                    Text(
                        text = distanceUnit,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Quick Adjust Steppers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-500, 500, 1000).forEach { delta ->
                    val label = if (delta > 0) "+$delta" else "$delta"
                    FilterChip(
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEvent(AddVehicleUiEvent.OdometerAdjusted(delta))
                        },
                        label = {
                            Text(
                                text = "$label $distanceUnit",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Starting Maintenance Plan Preview Deck
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.add_vehicle_schedule_preview_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(R.string.add_vehicle_schedule_preview_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Pre-seeded planned reminders
                    uiState.previewReminders.take(5).forEach { reminder ->
                        PlannedReminderPreviewRow(reminder = reminder)
                    }
                }
            }

            // Expandable Accordion for License Plate & VIN
            OutlinedCard(
                onClick = { showAdvanced = !showAdvanced },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.add_vehicle_advanced_accordion),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = showAdvanced) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.plateNumber,
                        onValueChange = { onEvent(AddVehicleUiEvent.PlateNumberChanged(it)) },
                        label = { Text("License plate") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.vin,
                        onValueChange = { onEvent(AddVehicleUiEvent.VinChanged(it)) },
                        label = { Text("VIN (17 characters)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (uiState.errorRes != null) {
                Text(
                    text = stringResource(uiState.errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onEvent(AddVehicleUiEvent.PreviousStepClicked) },
                modifier = Modifier
                    .weight(0.35f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back")
            }
            SaveButton(
                state = when {
                    uiState.isLoading -> SaveButtonState.Saving
                    uiState.isSaved -> SaveButtonState.Success
                    else -> SaveButtonState.Idle
                },
                onClick = { onEvent(AddVehicleUiEvent.SaveClicked) },
                text = stringResource(R.string.add_vehicle_save_cta),
                modifier = Modifier
                    .weight(0.65f)
                    .height(52.dp)
            )
        }
    }
}

@Composable
private fun PlannedReminderPreviewRow(reminder: PlannedReminder) {
    val distanceUnit = LocalDistanceUnit.current
    val formattedOdo = reminder.nextDueOdometer?.let {
        NumberFormat.getIntegerInstance().format(it)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(
                text = reminder.serviceType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = if (formattedOdo != null) "Due @ $formattedOdo $distanceUnit" else "Due in ${reminder.intervalDays} days",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = JetBrainsMono,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VehiclePhotoHero(
    photoUri: String?,
    onPickPhoto: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )
            )
            .clickable(onClick = onPickPhoto),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_change_photo),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Add vehicle photo (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
