package com.autominder.app.ui.screens.fuel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autominder.app.R
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.LoadingState
import com.autominder.app.ui.components.ListSkeleton
import com.autominder.app.ui.components.LocalSnackbarHostState
import com.autominder.app.ui.components.SwipeToDeleteContainer
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.launch
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: FuelHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.fuel_entry_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fuel_history_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
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
                uiState.isLoading -> ListSkeleton()
                errorRes != null -> ErrorState(
                    message = stringResource(errorRes),
                    onRetry = { viewModel.retry() }
                )
                uiState.entries.isEmpty() -> EmptyState(
                    title = stringResource(R.string.fuel_empty_history),
                    subtitle = "",
                    icon = Icons.Default.LocalGasStation,
                    hint = stringResource(R.string.empty_fuel_hint)
                )
                else -> FuelHistoryList(
                    uiState = uiState,
                    onDelete = { entry ->
                        viewModel.deleteEntry(entry)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = deletedMessage,
                                actionLabel = undoLabel,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoDelete(entry)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FuelHistoryList(
    uiState: FuelHistoryUiState,
    onDelete: (com.autominder.app.domain.model.FuelEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "efficiency_summary") {
            EfficiencyAtAGlance(uiState.averageEfficiency)
        }

        items(uiState.entries, key = { it.entry.id }) { item ->
            SwipeToDeleteContainer(
                onDelete = { onDelete(item.entry) },
                modifier = Modifier.animateItem()
            ) {
                FuelEntryCard(item = item)
            }
        }
    }
}

@Composable
private fun EfficiencyAtAGlance(average: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.fuel_efficiency_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                stringResource(R.string.fuel_efficiency_value_km_l, average),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FuelEntryCard(
    item: FuelEntryWithEfficiency,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                DateFormatUtil.formatDate(item.entry.date.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.mileage_log_odometer_value, item.entry.odometer),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMono)
                    )
                    Text(
                        "${item.entry.volumeMilliliters / 1000.0} L",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        currencyFormat.format(item.entry.costCents / 100.0),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMono),
                        fontWeight = FontWeight.Bold
                    )
                    item.efficiency?.let {
                        Text(
                            stringResource(R.string.fuel_efficiency_value_km_l, it),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (item.entry.notes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    item.entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
