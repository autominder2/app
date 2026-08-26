package com.autominder.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.VehicleOperationalStatus

/**
 * Universal AutoMinder Semantic Status Badge.
 *
 * Adheres to 2026 Material 3 Expressive & Accessibility Guidelines:
 * • NEVER relies on color alone — always pairs icon + text + shape
 * • Uses high-contrast WCAG-compliant color pairs
 * • Eliminates ambiguous "OK" terminology in favor of precise states
 */
@Composable
fun AutoMinderStatusBadge(
    status: VehicleOperationalStatus,
    modifier: Modifier = Modifier
) {
    val (labelRes, icon, containerColor, contentColor) = when (status) {
        VehicleOperationalStatus.HEALTHY -> StatusBadgeConfig(
            labelRes = R.string.state_all_clear,
            icon = Icons.Rounded.CheckCircle,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.tertiary
        )
        VehicleOperationalStatus.UPCOMING -> StatusBadgeConfig(
            labelRes = R.string.state_coming_up,
            icon = Icons.Rounded.Schedule,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        VehicleOperationalStatus.DUE_SOON -> StatusBadgeConfig(
            labelRes = R.string.state_due_soon,
            icon = Icons.Rounded.WarningAmber,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.secondary
        )
        VehicleOperationalStatus.OVERDUE -> StatusBadgeConfig(
            labelRes = R.string.state_overdue,
            icon = Icons.Rounded.ErrorOutline,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.error
        )
        VehicleOperationalStatus.SETUP_INCOMPLETE -> StatusBadgeConfig(
            labelRes = R.string.state_setup_incomplete,
            icon = Icons.Rounded.Speed,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Overload for single reminder / service status badges.
 */
@Composable
fun AutoMinderServiceStatusBadge(
    status: ServiceStatus,
    modifier: Modifier = Modifier
) {
    val (labelRes, icon, containerColor, contentColor) = when (status) {
        ServiceStatus.OVERDUE -> StatusBadgeConfig(
            labelRes = R.string.status_overdue,
            icon = Icons.Rounded.ErrorOutline,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.error
        )
        ServiceStatus.DUE_SOON -> StatusBadgeConfig(
            labelRes = R.string.status_due_soon,
            icon = Icons.Rounded.WarningAmber,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.secondary
        )
        ServiceStatus.SNOOZED -> StatusBadgeConfig(
            labelRes = R.string.status_snoozed,
            icon = Icons.Rounded.Schedule,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ServiceStatus.OK -> StatusBadgeConfig(
            labelRes = R.string.state_all_clear,
            icon = Icons.Rounded.CheckCircle,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.tertiary
        )
        ServiceStatus.COMPLETED -> StatusBadgeConfig(
            labelRes = R.string.status_completed,
            icon = Icons.Rounded.CheckCircle,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.tertiary
        )
        ServiceStatus.UNKNOWN -> StatusBadgeConfig(
            labelRes = R.string.status_unknown,
            icon = Icons.Rounded.Schedule,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

private data class StatusBadgeConfig(
    val labelRes: Int,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)
