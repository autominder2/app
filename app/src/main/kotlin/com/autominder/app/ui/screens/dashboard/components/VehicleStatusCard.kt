package com.autominder.app.ui.screens.dashboard.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.domain.usecase.PrioritizedReminder
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.screens.dashboard.VehicleOperationalStatus
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
                title = "Service coming up",
                subtitle = "One item is due soon.",
                isOverdue = false,
                modifier = modifier
            )
        }
        VehicleOperationalStatus.OVERDUE -> {
            AttentionBanner(
                title = "Attention needed",
                subtitle = "One or more maintenance items are overdue.",
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
    val greenBg = Color(0xFF10B981)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF0FDF4),
                        Color(0xFFDCFCE7).copy(alpha = 0.6f)
                    )
                )
            )
            .padding(18.dp)
            .semantics {
                contentDescription = "You're all caught up. Nothing needs attention right now."
            }
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = greenBg,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
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
                    color = Color(0xFF065F46)
                )

                Text(
                    text = stringResource(R.string.home_status_healthy_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF047857)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF059669).copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.home_status_updated_now),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF059669)
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
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
    val bgColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    val tintColor = if (isOverdue) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.tertiary

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
