package com.autominder.app.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.theme.Motion
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel

private const val STEP_WELCOME = 0
private const val STEP_ADD_CAR = 1
private const val STEP_PLAN = 2
private const val STEP_NOTIFY = 3
private const val STEP_COUNT = 4

private val POPULAR_MAKES = listOf(
    "Toyota", "Honda", "Ford", "Chevrolet", "Nissan", "Hyundai", "Kia",
    "Volkswagen", "BMW", "Mercedes", "Tesla", "Mazda", "Subaru", "Jeep"
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableIntStateOf(STEP_WELCOME) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

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
            // ── Top bar: segmented progress + Skip ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(STEP_COUNT) { index ->
                        val active = index <= step
                        val isCurrent = index == step
                        val barWidth by animateDpAsState(
                            targetValue = if (isCurrent) 32.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "progress$index"
                        )
                        val barColor by animateColorAsState(
                            targetValue = if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            label = "progressColor$index"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(barWidth)
                                .clip(CircleShape)
                                .background(barColor)
                        )
                    }
                }

                TextButton(onClick = { finish() }) {
                    Text(
                        text = stringResource(R.string.onboarding_skip_for_now),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Step content ────────────────────────────────────────────────
            AnimatedContent(
                targetState = step,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transitionSpec = {
                    val forward = targetState > initialState
                    val slideIn = slideInHorizontally { full ->
                        (if (forward) full else -full) / 3 * (if (Motion.amplitude > 0f) 1 else 0)
                    } + fadeIn()
                    val slideOut = slideOutHorizontally { full ->
                        (if (forward) -full else full) / 3 * (if (Motion.amplitude > 0f) 1 else 0)
                    } + fadeOut()
                    slideIn.togetherWith(slideOut)
                },
                label = "onboardingStep"
            ) { currentStep ->
                val distanceUnit = LocalDistanceUnit.current
                when (currentStep) {
                    STEP_WELCOME -> WelcomeStep(
                        onAddCar = { step = STEP_ADD_CAR }
                    )
                    STEP_ADD_CAR -> AddCarStep(
                        uiState = uiState,
                        onBrandChanged = viewModel::onBrandChanged,
                        onModelChanged = viewModel::onModelChanged,
                        onOdometerChanged = viewModel::onOdometerChanged,
                        onDrivingAmountChanged = viewModel::onDrivingAmountChanged,
                        onSeePlan = {
                            if (viewModel.previewPlan(distanceUnit)) step = STEP_PLAN
                        },
                        onLater = { finish() }
                    )
                    STEP_PLAN -> PlanStep(
                        uiState = uiState,
                        onOdometerChanged = viewModel::onOdometerChanged,
                        onDrivingAmountChanged = viewModel::onDrivingAmountChanged,
                        onRecompute = { viewModel.previewPlan(distanceUnit) },
                        onSave = viewModel::saveVehicle
                    )
                    STEP_NOTIFY -> NotifyStep(
                        onEnable = { requestNotificationsThenFinish() },
                        onLater = { finish() }
                    )
                }
            }
        }
    }
}

// ─── Step 0: Welcome ────────────────────────────────────────────────────────
@Composable
private fun WelcomeStep(onAddCar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            GlowHero(
                icon = Icons.Default.DirectionsCar,
                glowColor = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                fontFamily = Exo2,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.onboarding_welcome_value),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 3 Value Pillars (R&D Anti-Competitor 1-Star Guarantees)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ValuePillarCard(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.onboarding_pillar_privacy),
                    description = stringResource(R.string.onboarding_pillar_privacy_desc),
                    iconTint = MaterialTheme.colorScheme.primary
                )
                ValuePillarCard(
                    icon = Icons.Default.Timeline,
                    title = stringResource(R.string.onboarding_pillar_prediction),
                    description = stringResource(R.string.onboarding_pillar_prediction_desc),
                    iconTint = MaterialTheme.colorScheme.secondary
                )
                ValuePillarCard(
                    icon = Icons.Default.Bolt,
                    title = stringResource(R.string.onboarding_pillar_speed),
                    description = stringResource(R.string.onboarding_pillar_speed_desc),
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        PrimaryCta(
            text = stringResource(R.string.onboarding_add_my_car),
            onClick = onAddCar
        )
    }
}

@Composable
private fun ValuePillarCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Step 1: Add your car (Sticky Bottom Action) ─────────────────────────────
@Composable
private fun AddCarStep(
    uiState: OnboardingUiState,
    onBrandChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onOdometerChanged: (String) -> Unit,
    onDrivingAmountChanged: (DrivingAmount) -> Unit,
    onSeePlan: () -> Unit,
    onLater: () -> Unit
) {
    val distanceUnit = LocalDistanceUnit.current
    val canSave = uiState.brand.isNotBlank() && uiState.model.isNotBlank() && !uiState.isSaving
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_add_car_title),
                fontFamily = Exo2,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.onboarding_add_car_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Fast popular make pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(POPULAR_MAKES) { make ->
                    val isSelected = uiState.brand.equals(make, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onBrandChanged(make)
                        },
                        label = { Text(make, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                }
            }

            // Quick Model Suggestions for Selected Brand
            if (uiState.suggestedModels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(uiState.suggestedModels) { modelName ->
                        val isModelSelected = uiState.model.equals(modelName, ignoreCase = true)
                        FilterChip(
                            selected = isModelSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onModelChanged(modelName)
                            },
                            label = { Text(modelName, style = MaterialTheme.typography.labelMedium) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    OutlinedTextField(
                        value = uiState.brand,
                        onValueChange = onBrandChanged,
                        label = { Text(stringResource(R.string.label_make)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.model,
                        onValueChange = onModelChanged,
                        label = { Text(stringResource(R.string.label_model)) },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Label,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.odometer,
                        onValueChange = onOdometerChanged,
                        label = {
                            Text(
                                stringResource(
                                    R.string.label_current_odometer_dynamic,
                                    DistanceUtil.unitLabel(distanceUnit)
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = DistanceUtil.unitLabel(distanceUnit),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_driving_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            DrivingAmountChips(
                selected = uiState.drivingAmount,
                onSelected = onDrivingAmountChanged,
                distanceUnit = distanceUnit
            )

            if (uiState.errorRes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(uiState.errorRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sticky Bottom CTA Bar
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryCta(
                    text = stringResource(R.string.onboarding_see_my_plan),
                    onClick = onSeePlan,
                    enabled = canSave
                )
                TextButton(onClick = onLater) {
                    Text(
                        text = stringResource(R.string.onboarding_do_this_later),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Step 2: Plan reveal (Sticky Bottom Action) ─────────────────────────────
@Composable
private fun PlanStep(
    uiState: OnboardingUiState,
    onOdometerChanged: (String) -> Unit,
    onDrivingAmountChanged: (DrivingAmount) -> Unit,
    onRecompute: () -> Unit,
    onSave: () -> Unit
) {
    val distanceUnit = LocalDistanceUnit.current
    val unitLabel = DistanceUtil.unitLabel(distanceUnit)
    var showAdjust by rememberSaveable { mutableStateOf(false) }
    val first = uiState.plan.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_plan_title),
                fontFamily = Exo2,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.onboarding_plan_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Maintenance Card
            if (first != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.onboarding_plan_first_label).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = first.serviceType.localizedLabel(),
                            fontFamily = Exo2,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val intervalMonths = (first.intervalDays / 30).coerceAtLeast(1)
                        Text(
                            text = pluralStringResource(
                                R.plurals.onboarding_plan_why,
                                intervalMonths,
                                DistanceFormat.grouped(DistanceUtil.kmToDisplay(first.intervalKm, distanceUnit)),
                                unitLabel,
                                intervalMonths
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val dueOdometer = first.nextDueOdometer
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (dueOdometer != null) {
                                    stringResource(
                                        R.string.onboarding_plan_review_by,
                                        DateFormatUtil.formatDate(first.nextDueDate),
                                        DistanceFormat.grouped(
                                            DistanceUtil.kmToDisplay(dueOdometer, distanceUnit)
                                        ),
                                        unitLabel
                                    )
                                } else {
                                    stringResource(
                                        R.string.onboarding_plan_review_by_date,
                                        DateFormatUtil.formatDate(first.nextDueDate)
                                    )
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.plan.size > 1) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.onboarding_plan_more_title).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.plan.drop(1).forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.serviceType.localizedLabel(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = DateFormatUtil.formatDate(item.nextDueDate),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.onboarding_plan_honesty, uiState.brand.trim()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_plan_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = { showAdjust = !showAdjust }) {
                Text(stringResource(R.string.onboarding_plan_adjust))
            }
            if (showAdjust) {
                OutlinedTextField(
                    value = uiState.odometer,
                    onValueChange = {
                        onOdometerChanged(it)
                        onRecompute()
                    },
                    label = {
                        Text(
                            stringResource(
                                R.string.label_current_odometer_dynamic,
                                DistanceUtil.unitLabel(distanceUnit)
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = uiState.errorRes != null
                )
                if (uiState.errorRes != null) {
                    Text(
                        text = stringResource(uiState.errorRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                DrivingAmountChips(
                    selected = uiState.drivingAmount,
                    onSelected = {
                        onDrivingAmountChanged(it)
                        onRecompute()
                    },
                    distanceUnit = distanceUnit
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sticky Bottom CTA Bar
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                PrimaryCta(
                    text = stringResource(R.string.onboarding_plan_cta),
                    onClick = onSave,
                    enabled = uiState.planReady && !uiState.isSaving,
                    loading = uiState.isSaving
                )
            }
        }
    }
}

// ─── Step 3: Reminders, asked in context ────────────────────────────────────
@Composable
private fun NotifyStep(
    onEnable: () -> Unit,
    onLater: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlowHero(
            icon = Icons.Default.NotificationsActive,
            glowColor = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = stringResource(R.string.onboarding_notify_title),
            fontFamily = Exo2,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
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
        TextButton(onClick = onLater) {
            Text(
                text = stringResource(R.string.onboarding_maybe_later),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DrivingAmountChips(
    selected: DrivingAmount,
    onSelected: (DrivingAmount) -> Unit,
    distanceUnit: String
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DrivingAmount.entries.forEach { amount ->
            val labelRes = when (amount) {
                DrivingAmount.LOW -> R.string.driving_low
                DrivingAmount.TYPICAL -> R.string.driving_typical
                DrivingAmount.HIGH -> R.string.driving_high
            }
            val isSelected = selected == amount
            FilterChip(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onSelected(amount)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                label = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = stringResource(
                                R.string.driving_support_value,
                                DistanceFormat.grouped(
                                    DistanceUtil.kmToDisplay(amount.annualKm, distanceUnit)
                                ),
                                DistanceUtil.unitLabel(distanceUnit)
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

// ─── Shared UI Building Blocks ──────────────────────────────────────────────
@Composable
private fun GlowHero(
    icon: ImageVector,
    glowColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp)
    ) {
        // Outer halo
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.22f),
                            glowColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Mid halo
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = 0.15f))
        )
        // Core disc
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = glowColor
                )
            }
        }
    }
}

@Composable
private fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onClick()
        },
        enabled = enabled && !loading,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interactionSource),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
