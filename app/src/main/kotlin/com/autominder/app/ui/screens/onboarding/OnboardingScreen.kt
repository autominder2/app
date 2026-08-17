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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.usecase.PlannedReminder
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.theme.Motion
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel

// Activation-first onboarding: Welcome → Add your car → PLAN REVEAL → Reminders.
// The plan is visible BEFORE the notification ask: the user sees value first,
// then decides. Save happens on the reveal step's CTA, so denying the
// permission can never erase the plan.
private const val STEP_WELCOME = 0
private const val STEP_ADD_CAR = 1
private const val STEP_PLAN = 2
private const val STEP_NOTIFY = 3
private const val STEP_COUNT = 4

private val POPULAR_MAKES = listOf(
    "Toyota", "Honda", "Ford", "Chevrolet", "Nissan", "Hyundai", "Kia",
    "Volkswagen", "BMW", "Mercedes", "Tesla", "Mazda", "Suzuki", "Jeep"
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableIntStateOf(STEP_WELCOME) }
    val context = androidx.compose.ui.platform.LocalContext.current
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

    // Vehicle + plan saved from the reveal step → advance to notifications.
    LaunchedEffect(uiState.vehicleSaved) {
        if (uiState.vehicleSaved && step == STEP_PLAN) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            step = STEP_NOTIFY
        }
    }

    // Soft tick each time a step changes; back gesture steps backward.
    LaunchedEffect(step) {
        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }
    // Back walks the form/reveal steps; once saved (notify step), going back
    // would offer a Save that can no longer save, so back is disabled there.
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
                .imePadding()
        ) {
            // ── Top row: segmented progress + Skip ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp, top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(STEP_COUNT) { index ->
                        val active = index <= step
                        val barWidth by animateDpAsState(
                            targetValue = if (index == step) 28.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "progress$index"
                        )
                        val barColor by animateColorAsState(
                            targetValue = if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            label = "progressColor$index"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(barWidth)
                                .clip(CircleShape)
                                .background(barColor)
                        )
                    }
                }

                TextButton(onClick = { finish() }) {
                    Text(stringResource(R.string.onboarding_skip_for_now))
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
                        (if (forward) full else -full) / 4 * (if (Motion.amplitude > 0f) 1 else 0)
                    } + fadeIn()
                    val slideOut = slideOutHorizontally { full ->
                        (if (forward) -full else full) / 4 * (if (Motion.amplitude > 0f) 1 else 0)
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
            // The two centred steps were the only ones without scroll: at 2.0x
            // font scale the 180dp hero plus title, body and CTA exceed a
            // phone viewport, and a centred Column clips rather than scrolls.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlowHero(icon = Icons.Default.DirectionsCar)
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_value),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        PrimaryCta(
            text = stringResource(R.string.onboarding_add_my_car),
            onClick = onAddCar
        )
    }
}

// ─── Step 1: Add your car ───────────────────────────────────────────────────
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_add_car_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_add_car_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // One-tap make selection — least typing possible.
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(POPULAR_MAKES) { make ->
                FilterChip(
                    selected = uiState.brand.equals(make, ignoreCase = true),
                    onClick = { onBrandChanged(make) },
                    label = { Text(make) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.brand,
            onValueChange = onBrandChanged,
            label = { Text(stringResource(R.string.label_make)) },
            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.model,
            onValueChange = onModelChanged,
            label = { Text(stringResource(R.string.label_model)) },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
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
            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_driving_label),
            style = MaterialTheme.typography.labelLarge,
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

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSeePlan,
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_see_my_plan),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        TextButton(
            onClick = onLater,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.onboarding_do_this_later))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Step 2: Plan reveal — value BEFORE the permission ask ─────────────────
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_plan_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_plan_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        // What's first, why, and when — the verdict of this screen.
        if (first != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_plan_first_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = first.serviceType.localizedLabel(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // Without a starting odometer this plan has no distance axis
                // to promise against, so it promises only the date.
                val dueOdometer = first.nextDueOdometer
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
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (uiState.plan.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_plan_more_title),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            uiState.plan.drop(1).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.serviceType.localizedLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = DateFormatUtil.formatDate(item.nextDueDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Honesty: what this plan is, and what we don't know yet.
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_plan_honesty, uiState.brand.trim()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.onboarding_plan_missing),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Revise inputs without restarting onboarding.
        Spacer(modifier = Modifier.height(12.dp))
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

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSave,
            enabled = uiState.planReady && !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.onboarding_plan_cta),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Low / Typical / High with honest per-year values in the user's unit. */
@Composable
private fun DrivingAmountChips(
    selected: DrivingAmount,
    onSelected: (DrivingAmount) -> Unit,
    distanceUnit: String
) {
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
            FilterChip(
                selected = selected == amount,
                onClick = { onSelected(amount) },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Column {
                        Text(stringResource(labelRes))
                        Text(
                            text = stringResource(
                                R.string.driving_support_value,
                                DistanceFormat.grouped(
                                    DistanceUtil.kmToDisplay(amount.annualKm, distanceUnit)
                                ),
                                DistanceUtil.unitLabel(distanceUnit)
                            ),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    }
}

// ─── Step 2: Reminders, asked in context ────────────────────────────────────
@Composable
private fun NotifyStep(
    onEnable: () -> Unit,
    onLater: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Same large-font clipping risk as WelcomeStep.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlowHero(icon = Icons.Default.NotificationsActive)
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.onboarding_notify_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_notify_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        PrimaryCta(
            text = stringResource(R.string.onboarding_turn_on_reminders),
            onClick = onEnable
        )
        TextButton(onClick = onLater) {
            Text(stringResource(R.string.onboarding_maybe_later))
        }
    }
}

// ─── Shared pieces ──────────────────────────────────────────────────────────
@Composable
private fun GlowHero(icon: ImageVector) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PrimaryCta(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
