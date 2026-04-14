package com.autominder.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceStatus

@Composable
fun StatusChip(
    status: ServiceStatus,
    modifier: Modifier = Modifier
) {
    val (labelRes, containerColor, contentColor) = when (status) {
        ServiceStatus.OVERDUE -> Triple(
            R.string.status_overdue,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        ServiceStatus.DUE_SOON -> Triple(
            R.string.status_due_soon,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        ServiceStatus.SNOOZED -> Triple(
            R.string.status_snoozed,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        ServiceStatus.OK -> Triple(
            R.string.status_ok,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> Triple(
            R.string.status_ok, // Default to OK for now
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = stringResource(labelRes).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
