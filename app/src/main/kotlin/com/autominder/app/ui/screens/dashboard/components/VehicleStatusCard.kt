package com.autominder.app.ui.screens.dashboard.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.domain.model.VehicleOperationalStatus
import com.autominder.app.domain.usecase.PrioritizedReminder
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.util.DistanceFormat

@Composable
fun VehicleStatusCard(
    status: VehicleOperationalStatus,
    alertsCount: Int,
    nextCheck: PrioritizedReminder?,
    distanceUnit: String,
    onUpdateMileage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    when (status) {
        VehicleOperationalStatus.HEALTHY -> {
            HealthyBanner(modifier = modifier)
        }
        VehicleOperationalStatus.UPCOMING -> {
            UpcomingBanner(
                nextCheck = nextCheck,
                distanceUnit = distanceUnit,
                modifier = modifier
            )
        }
        VehicleOperationalStatus.DUE_SOON -> {
            AttentionBanner(
                title = stringResource(R.string.home_status_duesoon_title, 1),
                subtitle = stringResource(R.string.status_due_soon),
                isOverdue = false,
                modifier = modifier
            )
        }
        VehicleOperationalStatus.OVERDUE -> {
            AttentionBanner(
                title = stringResource(R.string.home_status_overdue_title, 1),
                subtitle = stringResource(R.string.home_subtitle_overdue),
                isOverdue = true,
                modifier = modifier
            )
        }
        VehicleOperationalStatus.SETUP_INCOMPLETE -> {
            SetupIncompleteBanner(
                onUpdateMileage = onUpdateMileage,
                interactionSource = interactionSource,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HealthyBanner(modifier: Modifier = Modifier) {
    val containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
    val tintColor = MaterialTheme.colorScheme.tertiary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "You're all caught up. Nothing needs attention right now."
            },
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = tintColor,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.home_status_healthy_title),
                    fontFamily = Exo2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.home_status_healthy_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.home_status_updated_now),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingBanner(
    nextCheck: PrioritizedReminder?,
    distanceUnit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_status_healthy_title),
                    fontFamily = Exo2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (nextCheck != null && nextCheck.remainingKm != null) {
                    val displayKm = DistanceUtil.kmToDisplay(nextCheck.remainingKm, distanceUnit)
                    val unitStr = DistanceUtil.unitLabel(distanceUnit)
                    Text(
                        text = "${nextCheck.categoryLabel} is your next check (~${DistanceFormat.grouped(displayKm)} $unitStr)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.home_status_healthy_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AttentionBanner(
    title: String,
    subtitle: String,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    // DUE_SOON = secondary slot (amber #9A6700). OVERDUE = error slot (red #B42318).
    // tertiary = HealthGreen — HEALTHY/OK/COMPLETED only. Never for warnings.
    val bgColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    val tintColor = if (isOverdue) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = tintColor.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontFamily = Exo2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SetupIncompleteBanner(
    onUpdateMileage: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onUpdateMileage,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_status_setup_incomplete_title),
                    fontFamily = Exo2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.home_status_update_mileage_cta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
