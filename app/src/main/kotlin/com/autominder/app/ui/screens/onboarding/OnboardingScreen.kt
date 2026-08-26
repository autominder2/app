package com.autominder.app.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.usecase.PlannedReminder
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.domain.util.VehicleCatalog
import com.autominder.app.ui.components.premium.InsightMetricCard
import com.autominder.app.ui.components.premium.InsightMetricRow
import com.autominder.app.ui.screens.onboarding.components.DrivingAmountChips
import com.autominder.app.ui.screens.onboarding.components.GlowHero
import com.autominder.app.ui.screens.onboarding.components.OnboardingProgressBar
import com.autominder.app.ui.screens.onboarding.components.PlanRevisionSheet
import com.autominder.app.ui.screens.onboarding.components.PrimaryCta
import com.autominder.app.ui.screens.onboarding.components.ValuePillarGroup
import com.autominder.app.ui.screens.onboarding.components.VehiclePickerMode
import com.autominder.app.ui.screens.onboarding.components.VehiclePickerSheet
import com.autominder.app.ui.theme.Dimensions
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel

private const val STEP_WELCOME = 0
private const val STEP_ADD_CAR = 1
private const val STEP_PLAN = 2
private const val STEP_NOTIFY = 3
private const val STEP_COUNT = 4

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableIntStateOf(STEP_WELCOME) }
    var showMakePickerSheet by rememberSaveable { mutableStateOf(false) }
    var showModelPickerSheet by rememberSaveable { mutableStateOf(false) }
    var showPlanRevisionSheet by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val distanceUnit = LocalDistanceUnit.current

    val notificationLauncher = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { _ ->
            viewModel.completeOnboarding()
            onFinished()
        }
    } else null

    fun finish() {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        viewModel.completeOnboarding()
        onFinished()
    }

    fun requestNotificationsThenFinish() {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && notificationLauncher != null) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.completeOnboarding()
        onFinished()
    }

    LaunchedEffect(uiState.vehicleSaved) {
        if (uiState.vehicleSaved && step == STEP_PLAN) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            step = STEP_NOTIFY
        }
    }

    LaunchedEffect(step) {
        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }

    BackHandler(enabled = step == STEP_ADD_CAR || step == STEP_PLAN) {
        step = if (step == STEP_PLAN) STEP_ADD_CAR else STEP_WELCOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Top bar: segmented progress + Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OnboardingProgressBar(
                    stepCount = STEP_COUNT,
                    currentStep = step
                )

                TextButton(
                    onClick = { finish() },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip_for_now),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Step Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (step) {
                    STEP_WELCOME -> WelcomeStep(
                        onNext = { step = STEP_ADD_CAR },
                        onSkip = { finish() }
                    )
                    STEP_ADD_CAR -> AddCarStep(
                        uiState = uiState,
                        onBrandChanged = { viewModel.onBrandChanged(it) },
                        onModelChanged = { viewModel.onModelChanged(it) },
                        onOdometerChanged = { viewModel.onOdometerChanged(it) },
                        onDrivingAmountChanged = { viewModel.onDrivingAmountChanged(it) },
                        onBrowseAllMakes = { showMakePickerSheet = true },
                        onBrowseAllModels = { showModelPickerSheet = true },
                        onSeePlan = {
                            if (viewModel.previewPlan(distanceUnit)) {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                step = STEP_PLAN
                            }
                        },
                        onSkip = { finish() },
                        distanceUnit = distanceUnit
                    )
                    STEP_PLAN -> PlanStep(
                        uiState = uiState,
                        distanceUnit = distanceUnit,
                        onAdjustPlan = { showPlanRevisionSheet = true },
                        onSavePlan = { viewModel.saveVehicle() },
                        onSkip = { finish() }
                    )
                    STEP_NOTIFY -> NotifyStep(
                        onEnable = { requestNotificationsThenFinish() },
                        onLater = { finish() }
                    )
                }
            }
        }

        // Bottom Sheets
        if (showMakePickerSheet) {
            VehiclePickerSheet(
                mode = VehiclePickerMode.MAKE,
                currentMake = uiState.brand,
                onSelect = { viewModel.onBrandChanged(it) },
                onDismiss = { showMakePickerSheet = false }
            )
        }

        if (showModelPickerSheet) {
            VehiclePickerSheet(
                mode = VehiclePickerMode.MODEL,
                currentMake = uiState.brand,
                onSelect = { viewModel.onModelChanged(it) },
                onDismiss = { showModelPickerSheet = false }
            )
        }

        if (showPlanRevisionSheet) {
            PlanRevisionSheet(
                odometer = uiState.odometer,
                onOdometerChanged = { viewModel.onOdometerChanged(it) },
                drivingAmount = uiState.drivingAmount,
                onDrivingAmountChanged = { viewModel.onDrivingAmountChanged(it) },
                distanceUnit = distanceUnit,
                onUpdatePlan = { viewModel.previewPlan(distanceUnit) },
                onDismiss = { showPlanRevisionSheet = false }
            )
        }
    }
}

// ─── Step 1: Welcome ────────────────────────────────────────────────────────
@Composable
private fun WelcomeStep(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.itemGapMedium)
    ) {
        GlowHero(
            icon = Icons.Rounded.DirectionsCar,
            glowColor = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.onboarding_tagline),
            fontFamily = Exo2,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics {
                heading()
                liveRegion = LiveRegionMode.Polite
            }
        )

        Text(
            text = stringResource(R.string.onboarding_welcome_value),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Redesigned: Single container card with 4 rows
        ValuePillarGroup()

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryCta(
            text = stringResource(R.string.onboarding_add_my_car),
            onClick = onNext
        )

        TextButton(
            onClick = onSkip,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_skip_for_now),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Step 2: Add Car ────────────────────────────────────────────────────────
@Composable
private fun AddCarStep(
    uiState: OnboardingUiState,
    onBrandChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onOdometerChanged: (String) -> Unit,
    onDrivingAmountChanged: (DrivingAmount) -> Unit,
    onBrowseAllMakes: () -> Unit,
    onBrowseAllModels: () -> Unit,
    onSeePlan: () -> Unit,
    onSkip: () -> Unit,
    distanceUnit: String
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(Dimensions.itemGapMedium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.onboarding_add_car_title),
                fontFamily = Exo2,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                }
            )
            Text(
                text = stringResource(R.string.onboarding_add_car_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Make input
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = uiState.brand,
                onValueChange = onBrandChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Make (e.g. Toyota, Honda)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp), // field shape convention
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            // Popular makes quick chips + Browse all trailing chip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = VehicleCatalog.popularMakes.take(6),
                    key = { it }
                ) { make ->
                    val isSelected = uiState.brand.equals(make, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onBrandChanged(make)
                        },
                        label = { Text(make, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                }

                item(key = "browse_all_makes_item") {
                    TextButton(
                        onClick = onBrowseAllMakes,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_browse_all_makes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Model input
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = uiState.model,
                onValueChange = onModelChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Model (e.g. RAV4, Civic)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp), // field shape convention
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            // Suggested models chips + Browse all models trailing chip
            if (uiState.suggestedModels.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = uiState.suggestedModels.take(5),
                        key = { it }
                    ) { model ->
                        val isSelected = uiState.model.equals(model, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onModelChanged(model)
                            },
                            label = { Text(model, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    }

                    item(key = "browse_all_models_item") {
                        TextButton(
                            onClick = onBrowseAllModels,
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_browse_all_models),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Odometer input + Reassurance caption
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = uiState.odometer,
                onValueChange = onOdometerChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Current odometer (optional)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Text(
                        text = DistanceUtil.unitLabel(distanceUnit),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp), // field shape convention
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            Text(
                text = stringResource(R.string.onboarding_odometer_reassurance),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        // Driving amount selector (Redesigned full-width selectable rows)
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.itemGapSmall)) {
            Text(
                text = stringResource(R.string.onboarding_driving_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            DrivingAmountChips(
                selected = uiState.drivingAmount,
                onSelected = onDrivingAmountChanged,
                distanceUnit = distanceUnit
            )
        }

        if (uiState.errorRes != null) {
            Text(
                text = stringResource(uiState.errorRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        PrimaryCta(
            text = stringResource(R.string.onboarding_see_my_plan),
            onClick = onSeePlan,
            enabled = uiState.canProceedFromAddCar
        )

        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_skip_for_now),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Step 3: Plan Preview ───────────────────────────────────────────────────
@Composable
private fun PlanStep(
    uiState: OnboardingUiState,
    distanceUnit: String,
    onAdjustPlan: () -> Unit,
    onSavePlan: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(Dimensions.itemGapMedium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.onboarding_plan_title),
                fontFamily = Exo2,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                }
            )
            Text(
                text = stringResource(R.string.onboarding_plan_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 2-up Stat Summary (Reusing InsightMetricRow / InsightMetricCard)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InsightMetricRow {
                val hasMileage = uiState.planOdometerKm != null && uiState.planOdometerKm > 0
                InsightMetricCard(
                    label = stringResource(R.string.onboarding_plan_metric_mileage),
                    value = if (hasMileage) {
                        DistanceFormat.grouped(DistanceUtil.kmToDisplay(uiState.planOdometerKm ?: 0, distanceUnit))
                    } else {
                        stringResource(R.string.onboarding_plan_metric_not_added)
                    },
                    unit = if (hasMileage) DistanceUtil.unitLabel(distanceUnit) else null,
                    icon = Icons.Rounded.Speed,
                    modifier = Modifier.weight(1f)
                )
                InsightMetricCard(
                    label = stringResource(R.string.onboarding_plan_metric_driving),
                    value = stringResource(
                        when (uiState.drivingAmount) {
                            DrivingAmount.LOW -> R.string.driving_light
                            DrivingAmount.TYPICAL -> R.string.driving_typical
                            DrivingAmount.HIGH -> R.string.driving_high
                        }
                    ),
                    icon = Icons.Rounded.DirectionsCar,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tappable adjust trigger
            TextButton(
                onClick = onAdjustPlan,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_plan_adjust),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Zero-mileage notice banner (when currentOdometer == 0)
        if (uiState.planOdometerKm == 0) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.onboarding_plan_mileage_not_added),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    // A TextButton, not a clickable Text.
                    //
                    // This was `Text(..., Modifier.clickable { })` on labelSmall
                    // type. A bare `clickable` adds no size floor and no
                    // accessibility role, so the tap target collapsed to the
                    // height of the glyphs. A uiautomator dump of the release
                    // build on a 1440x3120 device found a 39dp clickable node on
                    // this screen with nothing announced to TalkBack;
                    // .claude/rules/ui.md requires targets >= 48dp and
                    // meaningful semantics.
                    //
                    // TextButton supplies Role.Button and the ripple for free,
                    // and matches how every other action in this file is built.
                    // heightIn pushes past Material's 40dp button default to the
                    // 48dp the rules ask for.
                    TextButton(
                        onClick = onAdjustPlan,
                        modifier = Modifier.heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_plan_add_mileage_action),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Plan Reminders List
        if (uiState.plan.isNotEmpty()) {
            val first = uiState.plan.first()
            FirstReminderCard(
                reminder = first,
                distanceUnit = distanceUnit
            )

            val remaining = uiState.plan.drop(1)
            if (remaining.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.onboarding_plan_more_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        remaining.forEachIndexed { index, reminder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = reminder.serviceType.localizedLabel(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = DateFormatUtil.formatDate(reminder.nextDueDate),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (index < remaining.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.onboarding_plan_honesty, uiState.brand.ifBlank { "your car" }),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )

        Text(
            text = stringResource(R.string.onboarding_plan_missing),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )

        if (uiState.errorRes != null) {
            Text(
                text = stringResource(uiState.errorRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        PrimaryCta(
            text = stringResource(R.string.onboarding_plan_cta),
            onClick = onSavePlan,
            loading = uiState.isSaving
        )

        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_skip_for_now),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FirstReminderCard(
    reminder: PlannedReminder,
    distanceUnit: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_plan_first_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = reminder.serviceType.localizedLabel(),
                fontFamily = Exo2,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val nextDueOdo = reminder.nextDueOdometer
            val reviewText = if (nextDueOdo != null) {
                val display = DistanceFormat.grouped(DistanceUtil.kmToDisplay(nextDueOdo, distanceUnit))
                stringResource(
                    R.string.onboarding_plan_review_by,
                    DateFormatUtil.formatDate(reminder.nextDueDate),
                    display,
                    DistanceUtil.unitLabel(distanceUnit)
                )
            } else {
                stringResource(
                    R.string.onboarding_plan_review_by_date,
                    DateFormatUtil.formatDate(reminder.nextDueDate)
                )
            }

            Text(
                text = reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            val intervalKm = reminder.intervalKm
            val intervalDays = reminder.intervalDays
            val displayInterval = DistanceFormat.grouped(DistanceUtil.kmToDisplay(intervalKm, distanceUnit))
            val months = (intervalDays / 30).coerceAtLeast(1)
            Text(
                text = pluralStringResource(
                    R.plurals.onboarding_plan_why,
                    months,
                    displayInterval,
                    DistanceUtil.unitLabel(distanceUnit),
                    months
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Step 4: Notify ─────────────────────────────────────────────────────────
@Composable
private fun NotifyStep(
    onEnable: () -> Unit,
    onLater: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlowHero(
            icon = Icons.Rounded.NotificationsActive,
            glowColor = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = stringResource(R.string.onboarding_notify_title),
            fontFamily = Exo2,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics {
                heading()
                liveRegion = LiveRegionMode.Polite
            }
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.onboarding_notify_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        PrimaryCta(
            text = stringResource(R.string.onboarding_turn_on_reminders),
            onClick = onEnable
        )
        TextButton(
            onClick = onLater,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_maybe_later),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
