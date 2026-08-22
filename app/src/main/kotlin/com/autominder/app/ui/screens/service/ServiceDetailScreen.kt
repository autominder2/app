package com.autominder.app.ui.screens.service

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.autominder.app.R
import com.autominder.app.core.util.labelRes
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.ListSkeleton
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import com.autominder.app.ui.util.localizedLabel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ServiceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = stringResource(R.string.service_detail_delete_title),
                    fontFamily = Exo2,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.service_detail_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                        showDeleteConfirmation = false
                        viewModel.onEvent(ServiceDetailUiEvent.DeleteClicked)
                    }
                ) {
                    Text(
                        stringResource(R.string.service_detail_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.service_detail_title),
                        fontFamily = Exo2,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    val service = uiState.service
                    val vehicle = uiState.vehicle
                    if (service != null) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            shareServiceReceipt(context, service, vehicle)
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.service_detail_share_proof),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            showDeleteConfirmation = true
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.service_detail_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val errorRes = uiState.errorRes
            when {
                uiState.isLoading -> ListSkeleton(rows = 4)
                errorRes != null -> ErrorState(
                    message = stringResource(errorRes, *uiState.errorArgs.toTypedArray()),
                    onRetry = { viewModel.retry() }
                )
                uiState.service == null -> EmptyState(
                    title = stringResource(R.string.service_detail_not_found_title),
                    subtitle = stringResource(R.string.service_detail_not_found_subtitle),
                    icon = Icons.Default.Build
                )
                else -> ServiceDetailBentoContent(
                    service = uiState.service!!,
                    vehicle = uiState.vehicle,
                    onShareClick = {
                        shareServiceReceipt(context, uiState.service!!, uiState.vehicle)
                    }
                )
            }
        }
    }
}

/**
 * 2026 Material 3 Expressive Bento Grid Service Detail & Receipt Cockpit
 */
@Composable
private fun ServiceDetailBentoContent(
    service: Service,
    vehicle: Vehicle?,
    onShareClick: () -> Unit
) {
    val distanceUnit = LocalDistanceUnit.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val serviceTitle = if (service.serviceType == ServiceType.CUSTOM) {
        service.customLabel ?: stringResource(R.string.service_detail_custom_service)
    } else {
        service.serviceType.localizedLabel()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Digital Proof-of-Service Receipt Hero
        item(key = "receipt_hero") {
            ElevatedCard(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = serviceIconFor(service.serviceType),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Formatted Cost Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (service.costCents != null && service.costCents > 0) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ) {
                            Text(
                                text = if (service.costCents != null && service.costCents > 0) {
                                    currencyFormat.format(service.costCents / 100.0)
                                } else {
                                    stringResource(R.string.service_detail_no_cost)
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold,
                                color = if (service.costCents != null && service.costCents > 0) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = serviceTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = Exo2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateFormatUtil.formatDate(service.serviceDate),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Vehicle Context Digital Twin Card
        if (vehicle != null) {
            item(key = "vehicle_context") {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.service_detail_vehicle_title).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.vehicle_make_model, vehicle.make, vehicle.model),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = Exo2,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (vehicle.year > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = vehicle.year.toString(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 2-Column Precision Telemetry Bento Grid
        item(key = "telemetry_bento") {
            val formattedOdometer = DistanceFormat.grouped(
                DistanceUtil.kmToDisplay(service.odometerAtService, distanceUnit)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Odometer Stamp Bento Card
                ServiceTelemetryPill(
                    title = stringResource(R.string.service_detail_odometer).uppercase(),
                    value = "$formattedOdometer ${DistanceUtil.unitLabel(distanceUnit)}",
                    subtitle = stringResource(R.string.telemetry_and_date_title),
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )

                // Workshop / Provider Bento Card
                ServiceTelemetryPill(
                    title = stringResource(R.string.service_detail_shop).uppercase(),
                    value = if (!service.shopName.isNullOrBlank()) {
                        service.shopName
                    } else {
                        stringResource(R.string.service_detail_diy_workshop)
                    },
                    subtitle = stringResource(R.string.cost_and_shop_title),
                    icon = Icons.Default.Storefront,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Service Notes & Work Log Card (if present)
        if (service.notes.isNotBlank()) {
            item(key = "notes_card") {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.service_detail_notes_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = service.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Proof-of-Service Receipt Photo Card (if attached)
        if (!service.receiptPhotoUri.isNullOrBlank()) {
            item(key = "receipt_photo") {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.service_detail_receipt),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(service.receiptPhotoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.service_detail_receipt),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // 6. Action Button: Share Proof of Service
        item(key = "share_action") {
            Spacer(modifier = Modifier.height(6.dp))
            FilledTonalButton(
                onClick = onShareClick,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.service_detail_share_proof),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Precision Telemetry Pill Box
 */
@Composable
private fun ServiceTelemetryPill(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Service Icon Selector Helper
 */
private fun serviceIconFor(type: ServiceType): ImageVector = when (type) {
    ServiceType.OIL_CHANGE -> Icons.Default.Build
    ServiceType.TIRE_ROTATION -> Icons.Default.Speed
    ServiceType.BRAKE_SERVICE -> Icons.Default.DirectionsCar
    ServiceType.BATTERY -> Icons.Default.Build
    ServiceType.AIR_FILTER, ServiceType.CABIN_FILTER -> Icons.Default.Build
    ServiceType.TRANSMISSION, ServiceType.COOLANT -> Icons.Default.LocalGasStation
    ServiceType.SPARK_PLUGS, ServiceType.TIMING_BELT -> Icons.Default.Build
    ServiceType.WIPER_BLADES -> Icons.Default.Build
    ServiceType.INSURANCE, ServiceType.REGISTRATION, ServiceType.INSPECTION, ServiceType.EMISSIONS_TEST -> Icons.Default.Receipt
    ServiceType.CUSTOM -> Icons.Default.Build
}

/**
 * Share formatted service receipt summary via Android Intent Sharesheet
 */
private fun shareServiceReceipt(context: Context, service: Service, vehicle: Vehicle?) {
    val serviceName = if (service.serviceType == ServiceType.CUSTOM) {
        service.customLabel ?: context.getString(R.string.service_detail_custom_service)
    } else {
        context.getString(service.serviceType.labelRes())
    }
    val vehicleName = vehicle?.let {
        context.getString(R.string.vehicle_make_model, it.make, it.model)
    } ?: context.getString(R.string.label_unknown_vehicle)

    val dateFormatted = DateFormatUtil.formatDate(service.serviceDate)
    val distanceUnit = "km"
    val odometerFormatted = DistanceFormat.grouped(service.odometerAtService)
    val costFormatted = if (service.costCents != null && service.costCents > 0) {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(service.costCents / 100.0)
    } else {
        context.getString(R.string.service_detail_no_cost)
    }
    val shopFormatted = if (!service.shopName.isNullOrBlank()) {
        service.shopName
    } else {
        context.getString(R.string.service_detail_diy_workshop)
    }
    val notesFormatted = if (service.notes.isNotBlank()) service.notes else "—"

    val shareBody = context.getString(
        R.string.service_detail_share_body,
        serviceName,
        vehicleName,
        dateFormatted,
        odometerFormatted,
        distanceUnit,
        costFormatted,
        shopFormatted,
        notesFormatted
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.service_detail_share_title, serviceName, vehicleName))
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.service_detail_share_proof)))
}
