package com.autominder.app.ui.screens.quote

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.domain.model.QuoteAuditResult
import com.autominder.app.domain.model.QuoteItem
import com.autominder.app.domain.model.QuoteLineVerdict
import com.autominder.app.domain.model.QuoteVerdictStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.ListSkeleton
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import java.text.NumberFormat
import java.util.Locale

private val commonQuickAddTypes = listOf(
    ServiceType.OIL_CHANGE to 6500,
    ServiceType.CABIN_FILTER to 4500,
    ServiceType.AIR_FILTER to 4000,
    ServiceType.BRAKE_SERVICE to 22000,
    ServiceType.COOLANT to 14000,
    ServiceType.TRANSMISSION to 22000,
    ServiceType.BATTERY to 18000,
    ServiceType.SPARK_PLUGS to 18000,
    ServiceType.TIRE_ROTATION to 3500,
    ServiceType.WIPER_BLADES to 3500
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteAuditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuoteAuditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedSuccessMessage) {
        uiState.savedSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(QuoteAuditorUiEvent.ClearSavedMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.quote_auditor_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = Exo2,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = stringResource(R.string.quote_auditor_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (uiState.items.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.onEvent(QuoteAuditorUiEvent.ResetQuote) }
                        ) {
                            Text(stringResource(R.string.quote_auditor_clear_all))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (uiState.auditResult != null && uiState.items.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onEvent(QuoteAuditorUiEvent.SaveApprovedServices) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.quote_auditor_action_save_approved),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ListSkeleton(rows = 5, modifier = Modifier.padding(innerPadding))
            }
            uiState.errorRes != null -> {
                ErrorState(
                    message = stringResource(uiState.errorRes!!),
                    onRetry = { viewModel.onEvent(QuoteAuditorUiEvent.ResetQuote) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                QuoteAuditorContent(
                    uiState = uiState,
                    onSelectVehicle = { viewModel.onEvent(QuoteAuditorUiEvent.SelectVehicle(it)) },
                    onAddItem = { type, price -> viewModel.onEvent(QuoteAuditorUiEvent.AddItem(type, price)) },
                    onRemoveItem = { viewModel.onEvent(QuoteAuditorUiEvent.RemoveItem(it)) },
                    onUpdatePrice = { id, price -> viewModel.onEvent(QuoteAuditorUiEvent.UpdateItemPrice(id, price)) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun QuoteAuditorContent(
    uiState: QuoteAuditorUiState,
    onSelectVehicle: (Long) -> Unit,
    onAddItem: (ServiceType, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onUpdatePrice: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Garage Vehicle Selector ──
        if (uiState.vehicles.size > 1) {
            item(key = "vehicle_selector") {
                VehicleSelectorRow(
                    vehicles = uiState.vehicles,
                    selectedVehicleId = uiState.selectedVehicle?.id,
                    onSelectVehicle = onSelectVehicle
                )
            }
        }

        // ── 2. Hero Verdict Bento Card ──
        if (uiState.auditResult != null && uiState.items.isNotEmpty()) {
            item(key = "hero_verdict") {
                HeroVerdictBentoCard(
                    result = uiState.auditResult,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── 3. Mechanic Talking Points Script ──
            if (uiState.auditResult.mechanicTalkingPoints.isNotEmpty()) {
                item(key = "talking_points") {
                    MechanicTalkingPointsCard(
                        points = uiState.auditResult.mechanicTalkingPoints,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // ── 4. Quick Add Common Quote Items ──
        item(key = "quick_add_chips") {
            QuickAddChipsSection(
                onAddItem = onAddItem,
                currentItems = uiState.items,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // ── 5. Audited Line Items List or Empty State ──
        if (uiState.items.isEmpty()) {
            item(key = "empty_state") {
                EmptyState(
                    title = stringResource(R.string.quote_auditor_empty_title),
                    subtitle = stringResource(R.string.quote_auditor_empty_subtitle),
                    hint = stringResource(R.string.quote_auditor_empty_hint),
                    icon = Icons.Filled.Security,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            item(key = "items_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.quote_auditor_add_item_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Exo2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.quote_auditor_items_count, uiState.items.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val verdictsMap = uiState.auditResult?.lineVerdicts?.associateBy { it.item.id } ?: emptyMap()

            items(
                items = uiState.items,
                key = { it.id }
            ) { item ->
                val verdict = verdictsMap[item.id]
                QuoteLineItemCard(
                    item = item,
                    verdict = verdict,
                    onRemove = { onRemoveItem(item.id) },
                    onUpdatePrice = { price -> onUpdatePrice(item.id, price) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun VehicleSelectorRow(
    vehicles: List<Vehicle>,
    selectedVehicleId: Long?,
    onSelectVehicle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        vehicles.forEach { vehicle ->
            val isSelected = vehicle.id == selectedVehicleId
            FilterChip(
                selected = isSelected,
                onClick = { onSelectVehicle(vehicle.id) },
                label = {
                    Text(
                        text = com.autominder.app.domain.util.VehicleDisplayNameFormatter.format(vehicle.make, vehicle.model, vehicle.year),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun HeroVerdictBentoCard(
    result: QuoteAuditResult,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val totalQuotedFormatted = remember(result.totalQuotedCents) {
        currencyFormat.format(result.totalQuotedCents / 100.0)
    }
    val fairMinFormatted = remember(result.fairPriceMinCents) {
        currencyFormat.format(result.fairPriceMinCents / 100.0)
    }
    val fairMaxFormatted = remember(result.fairPriceMaxCents) {
        currencyFormat.format(result.fairPriceMaxCents / 100.0)
    }
    val savingsFormatted = remember(result.potentialSavingsCents) {
        currencyFormat.format(result.potentialSavingsCents / 100.0)
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.quote_auditor_hero_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Exo2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (result.potentialSavingsCents > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1B5E20).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Save $savingsFormatted",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.quote_auditor_total_quoted),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalQuotedFormatted,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.quote_auditor_fair_range),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$fairMinFormatted – $fairMaxFormatted",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Verdict Breakdown Pills ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VerdictCountPill(
                    count = result.legitimateItemsCount,
                    label = "Legitimate",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                VerdictCountPill(
                    count = result.verifyItemsCount,
                    label = "Verify",
                    color = Color(0xFFF57F17),
                    modifier = Modifier.weight(1f)
                )
                VerdictCountPill(
                    count = result.upsellItemsCount,
                    label = "Upsell",
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VerdictCountPill(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun MechanicTalkingPointsCard(
    points: List<String>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.quote_auditor_talking_points_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = Exo2,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.quote_auditor_talking_points_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            points.forEachIndexed { index, point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAddChipsSection(
    onAddItem: (ServiceType, Int) -> Unit,
    currentItems: List<QuoteItem>,
    modifier: Modifier = Modifier
) {
    val existingTypes = currentItems.map { it.serviceType }.toSet()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.quote_auditor_quick_add_title),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonQuickAddTypes.forEach { (type, defaultPrice) ->
                val isAdded = existingTypes.contains(type)
                FilterChip(
                    selected = isAdded,
                    onClick = {
                        if (!isAdded) onAddItem(type, defaultPrice)
                    },
                    label = { Text(type.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isAdded) Icons.Filled.CheckCircle else Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun QuoteLineItemCard(
    item: QuoteItem,
    verdict: QuoteLineVerdict?,
    onRemove: () -> Unit,
    onUpdatePrice: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    var priceText by remember(item.priceCents) {
        mutableStateOf(if (item.priceCents > 0) (item.priceCents / 100).toString() else "")
    }

    val statusColor = when (verdict?.status) {
        QuoteVerdictStatus.LEGITIMATE_DUE -> Color(0xFF2E7D32)
        QuoteVerdictStatus.VERIFY_FIRST -> Color(0xFFF57F17)
        QuoteVerdictStatus.LIKELY_UPSELL -> Color(0xFFC62828)
        QuoteVerdictStatus.CAN_WAIT, null -> MaterialTheme.colorScheme.primary
    }

    val icon = when (item.serviceType) {
        ServiceType.OIL_CHANGE -> Icons.Filled.Opacity
        ServiceType.TIRE_ROTATION -> Icons.Filled.Sync
        ServiceType.BRAKE_SERVICE -> Icons.Filled.Warning
        ServiceType.BATTERY -> Icons.Filled.ElectricBolt
        ServiceType.AIR_FILTER, ServiceType.CABIN_FILTER -> Icons.Filled.Air
        ServiceType.TRANSMISSION, ServiceType.COOLANT -> Icons.Filled.Settings
        ServiceType.SPARK_PLUGS -> Icons.Filled.Build
        else -> Icons.Filled.Build
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.customLabel ?: item.serviceType.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (verdict != null) {
                            Text(
                                text = stringResource(verdict.status.titleRes),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            )
                        }
                    }
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_delete_item),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Price Input & Fair Price Badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        val clean = newValue.filter { it.isDigit() }
                        priceText = clean
                        val cents = (clean.toIntOrNull() ?: 0) * 100
                        onUpdatePrice(cents)
                    },
                    label = { Text(stringResource(R.string.quote_auditor_item_price_hint)) },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(140.dp)
                )

                if (verdict != null) {
                    val fairMin = currencyFormat.format(verdict.fairPriceRangeCents.first / 100.0)
                    val fairMax = currencyFormat.format(verdict.fairPriceRangeCents.last / 100.0)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = stringResource(R.string.quote_auditor_fair_price_badge, fairMin, fairMax),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (verdict != null) {
                Spacer(modifier = Modifier.height(12.dp))
                // ── Reason ──
                Text(
                    text = verdict.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Counter Question Box ──
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = verdict.questionToAsk,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
