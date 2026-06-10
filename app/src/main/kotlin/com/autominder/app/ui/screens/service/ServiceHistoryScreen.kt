package com.autominder.app.ui.screens.service

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autominder.app.R
import com.autominder.app.ui.components.EmptyState
import com.autominder.app.ui.components.ErrorState
import com.autominder.app.ui.components.LoadingState
import java.text.NumberFormat
import com.autominder.app.ui.util.DateFormatUtil
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceHistoryScreen(
    onNavigateToServiceDetail: (Long) -> Unit,
    viewModel: ServiceHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.service_history_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            when (val state = uiState) {
                is ServiceHistoryUiState.Loading -> LoadingState()
                is ServiceHistoryUiState.Empty -> EmptyState(
                    title = stringResource(R.string.service_history_empty_title),
                    subtitle = stringResource(R.string.service_history_empty_subtitle),
                    hint = stringResource(R.string.empty_service_hint)
                )
                is ServiceHistoryUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
                is ServiceHistoryUiState.Success -> ServiceHistoryContent(
                    groups = state.groups,
                    onServiceClick = onNavigateToServiceDetail
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ServiceHistoryContent(
    groups: List<ServiceGroup>,
    onServiceClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            stickyHeader(key = "header_${group.monthYear}") {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = group.monthYear,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            items(
                items = group.services,
                key = { it.service.id }
            ) { serviceWithVehicle ->
                ServiceHistoryCard(
                    serviceWithVehicle = serviceWithVehicle,
                    onClick = { onServiceClick(serviceWithVehicle.service.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun ServiceHistoryCard(
    serviceWithVehicle: ServiceWithVehicle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val service = serviceWithVehicle.service
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.customLabel ?: service.serviceType.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (service.costCents != null) {
                    Text(
                        text = currencyFormat.format(service.costCents / 100.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = serviceWithVehicle.vehicleName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = DateFormatUtil.formatDate(service.serviceDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
