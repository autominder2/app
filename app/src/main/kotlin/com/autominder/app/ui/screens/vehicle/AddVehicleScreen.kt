package com.autominder.app.ui.screens.vehicle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.GlobalScope
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
            val adManager = activity?.adManager
            if (adManager != null && activity != null && adManager.shouldShowInterstitial()) {
                adManager.showInterstitial(activity) { onNavigateBack() }
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photo picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
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
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = stringResource(R.string.cd_add_photo),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.action_add_photo), color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        OutlinedTextField(
            value = uiState.brand,
            onValueChange = { onEvent(AddVehicleUiEvent.BrandChanged(it)) },
            label = { Text(stringResource(R.string.label_brand)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.model,
            onValueChange = { onEvent(AddVehicleUiEvent.ModelChanged(it)) },
            label = { Text(stringResource(R.string.label_model)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.year,
            onValueChange = { onEvent(AddVehicleUiEvent.YearChanged(it)) },
            label = { Text(stringResource(R.string.label_year)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.plateNumber,
            onValueChange = { onEvent(AddVehicleUiEvent.PlateNumberChanged(it)) },
            label = { Text(stringResource(R.string.label_plate_number)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.vin,
            onValueChange = { onEvent(AddVehicleUiEvent.VinChanged(it)) },
            label = { Text(stringResource(R.string.label_vin_optional)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.currentOdometer,
            onValueChange = { onEvent(AddVehicleUiEvent.OdometerChanged(it)) },
            label = { Text(stringResource(R.string.label_current_odometer_dynamic, DistanceUtil.unitLabel(LocalDistanceUnit.current))) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

    }
}
