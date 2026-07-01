package com.autominder.app.ui.screens.vehicle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.autominder.app.ui.components.FormField
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
import com.autominder.app.MainActivity
import com.autominder.app.R
import com.autominder.app.domain.util.DistanceUtil
import androidx.compose.material3.SnackbarDuration
import com.autominder.app.ui.components.LocalSnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.components.LoadingState

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
            val activity = context as? MainActivity
            if (activity != null) {
                val adManager = activity.adManager
                if (adManager.shouldShowInterstitial()) {
                    adManager.showInterstitial(activity) { onNavigateBack() }
                } else {
                    onNavigateBack()
                }
            } else {
                onNavigateBack()
            }
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
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(AddVehicleUiEvent.SaveClicked) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.action_save))
                    }
                }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            label = "ScreenState",
            modifier = Modifier.padding(padding)
        ) { isLoading ->
            if (isLoading) {
                LoadingState()
            } else {
                AddVehicleForm(
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
    }
}

@Composable
private fun AddVehicleForm(
    uiState: AddVehicleUiState,
    onEvent: (AddVehicleUiEvent) -> Unit,
    onPickPhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero photo picker — the emotional anchor of the form
        FormField(index = 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = stringResource(R.string.cd_add_photo),
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.action_add_photo),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.add_vehicle_photo_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        FormSectionLabel(stringResource(R.string.add_vehicle_section_vehicle))

        FormField(index = 1) {
            VehicleField(
                value = uiState.brand,
                onValueChange = { onEvent(AddVehicleUiEvent.BrandChanged(it)) },
                label = stringResource(R.string.label_brand),
                icon = Icons.Default.DirectionsCar
            )
        }
        FormField(index = 2) {
            VehicleField(
                value = uiState.model,
                onValueChange = { onEvent(AddVehicleUiEvent.ModelChanged(it)) },
                label = stringResource(R.string.label_model),
                icon = Icons.AutoMirrored.Filled.Label
            )
        }
        FormField(index = 3) {
            VehicleField(
                value = uiState.year,
                onValueChange = { onEvent(AddVehicleUiEvent.YearChanged(it)) },
                label = stringResource(R.string.label_year),
                icon = Icons.Default.CalendarMonth,
                keyboardType = KeyboardType.Number
            )
        }

        FormSectionLabel(stringResource(R.string.add_vehicle_section_identity))

        FormField(index = 4) {
            VehicleField(
                value = uiState.plateNumber,
                onValueChange = { onEvent(AddVehicleUiEvent.PlateNumberChanged(it)) },
                label = stringResource(R.string.label_plate_number),
                icon = Icons.Default.Badge
            )
        }
        FormField(index = 5) {
            VehicleField(
                value = uiState.vin,
                onValueChange = { onEvent(AddVehicleUiEvent.VinChanged(it)) },
                label = stringResource(R.string.label_vin_optional),
                icon = Icons.Default.Numbers
            )
        }

        FormSectionLabel(stringResource(R.string.add_vehicle_section_odometer))

        FormField(index = 6) {
            VehicleField(
                value = uiState.currentOdometer,
                onValueChange = { onEvent(AddVehicleUiEvent.OdometerChanged(it)) },
                label = stringResource(R.string.label_current_odometer_dynamic, DistanceUtil.unitLabel(LocalDistanceUnit.current)),
                icon = Icons.Default.Speed,
                keyboardType = KeyboardType.Number
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FormSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
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
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
