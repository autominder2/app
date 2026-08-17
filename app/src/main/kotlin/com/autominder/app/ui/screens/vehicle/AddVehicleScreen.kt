package com.autominder.app.ui.screens.vehicle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.autominder.app.ui.components.SaveButton
import com.autominder.app.ui.components.SaveButtonState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.autominder.app.R
import com.autominder.app.domain.util.DistanceUtil
import androidx.compose.material3.SnackbarDuration
import com.autominder.app.ui.components.LocalSnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.autominder.app.ui.theme.LocalDistanceUnit

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
        uiState.plateNumber.isNotBlank() ||
        uiState.vin.isNotBlank() ||
        uiState.photoUri != null

    val onBackRequest: () -> Unit = {
        if (hasUnsavedChanges && !uiState.isSaved) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    androidx.activity.compose.BackHandler(enabled = hasUnsavedChanges && !uiState.isSaved) {
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
                /* Log or ignore, continue with uri */
            }
            viewModel.onEvent(AddVehicleUiEvent.PhotoUriChanged(it.toString())) 
        } 
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            keyboardController?.hide()
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.vehicle_saved),
                duration = SnackbarDuration.Short
            )
            // No interstitial here: first vehicle save is the highest-churn
            // trust moment (Gate A decision — ads never on decision surfaces).
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_vehicle_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackRequest) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        AddVehicleForm(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onPickPhoto = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

// Tap-don't-type: the makes most people actually drive, so the common case is
// one tap instead of typing.
private val POPULAR_MAKES = listOf(
    "Toyota", "Honda", "Ford", "Chevrolet", "Nissan", "Hyundai", "Kia",
    "Volkswagen", "BMW", "Mercedes", "Tesla", "Mazda", "Suzuki", "Jeep"
)

@Composable
private fun AddVehicleForm(
    uiState: AddVehicleUiState,
    onEvent: (AddVehicleUiEvent) -> Unit,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMore by remember { mutableStateOf(false) }
    val canSave = uiState.brand.isNotBlank() && uiState.model.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warm, single-question header — sets the "this is quick" tone
        Text(
            text = stringResource(R.string.add_vehicle_headline),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.add_vehicle_subhead),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // One-tap make selection
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(POPULAR_MAKES) { make ->
                FilterChip(
                    selected = uiState.brand.equals(make, ignoreCase = true),
                    onClick = { onEvent(AddVehicleUiEvent.BrandChanged(make)) },
                    label = { Text(make) }
                )
            }
        }

        VehicleField(
            value = uiState.brand,
            onValueChange = { onEvent(AddVehicleUiEvent.BrandChanged(it)) },
            label = stringResource(R.string.label_make),
            icon = Icons.Default.DirectionsCar
        )
        VehicleField(
            value = uiState.model,
            onValueChange = { onEvent(AddVehicleUiEvent.ModelChanged(it)) },
            label = stringResource(R.string.label_model),
            icon = Icons.AutoMirrored.Filled.Label
        )

        // The magic reassurance — the app remembers reminders so users don't
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.add_vehicle_auto_reminders),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        // Everything else is opt-in, folded away so the default view stays tiny
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PhotoPicker(uiState = uiState, onPickPhoto = onPickPhoto)

                // Year quick-pick — tap a recent year instead of typing
                Text(
                    text = stringResource(R.string.label_year),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((0..12).toList()) { offset ->
                        val year = (currentYear - offset).toString()
                        FilterChip(
                            selected = uiState.year == year,
                            onClick = { onEvent(AddVehicleUiEvent.YearChanged(year)) },
                            label = { Text(year) }
                        )
                    }
                }

                VehicleField(
                    value = uiState.currentOdometer,
                    onValueChange = { onEvent(AddVehicleUiEvent.OdometerChanged(it)) },
                    label = stringResource(R.string.label_current_odometer_dynamic, DistanceUtil.unitLabel(LocalDistanceUnit.current)),
                    icon = Icons.Default.Speed,
                    keyboardType = KeyboardType.Number
                )
                VehicleField(
                    value = uiState.plateNumber,
                    onValueChange = { onEvent(AddVehicleUiEvent.PlateNumberChanged(it)) },
                    label = stringResource(R.string.label_plate_number),
                    icon = Icons.Default.Badge
                )
                VehicleField(
                    value = uiState.vin,
                    onValueChange = { onEvent(AddVehicleUiEvent.VinChanged(it)) },
                    label = stringResource(R.string.label_vin_optional),
                    icon = Icons.Default.Numbers
                )
            }
        }

        uiState.errorRes?.let { errorRes ->
            Text(
                text = stringResource(errorRes, *uiState.errorArgs.toTypedArray()),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        SaveButton(
            state = when {
                uiState.isSaved -> SaveButtonState.Success
                uiState.isLoading -> SaveButtonState.Saving
                else -> SaveButtonState.Idle
            },
            text = stringResource(R.string.add_vehicle_cta),
            onClick = { onEvent(AddVehicleUiEvent.SaveClicked) },
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PhotoPicker(uiState: AddVehicleUiState, onPickPhoto: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onPickPhoto() },
        contentAlignment = Alignment.Center
    ) {
        if (uiState.photoUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(uiState.photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.cd_vehicle_photo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_change_photo), style = MaterialTheme.typography.labelMedium)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = stringResource(R.string.cd_add_photo),
                        modifier = Modifier.padding(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.action_add_photo),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun VehicleField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
