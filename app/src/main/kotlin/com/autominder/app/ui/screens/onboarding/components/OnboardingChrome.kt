package com.autominder.app.ui.screens.onboarding.components

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autominder.app.R
import com.autominder.app.ui.components.pressScale
import com.autominder.app.ui.theme.Dimensions
import com.autominder.app.ui.theme.Motion

@Composable
fun GlowHero(
    icon: ImageVector,
    glowColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(170.dp)
    ) {
        // Outer halo
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.22f),
                            glowColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Mid halo
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = 0.15f))
        )
        // Core disc
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = glowColor
                )
            }
        }
    }
}

@Composable
fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onClick()
        },
        enabled = enabled && !loading,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interactionSource),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingProgressBar(
    stepCount: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(stepCount) { index ->
            val isCurrent = index == currentStep
            val barWidth by animateDpAsState(
                targetValue = if (isCurrent) 32.dp else 8.dp,
                animationSpec = Motion.springSnappy(),
                label = "progress_$index"
            )
            val barColor = if (index <= currentStep) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(barWidth)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

/**
 * Redesigned value pillars: collapses 4 separate elevated Surface cards into
 * a single surfaceContainerLow container with 1dp outlineVariant dividers.
 */
@Composable
fun ValuePillarGroup(
    modifier: Modifier = Modifier
) {
    val pillars = listOf(
        PillarItem(
            icon = Icons.Rounded.Build,
            tint = MaterialTheme.colorScheme.primary,
            titleRes = R.string.onboarding_pillar_maintenance,
            descRes = R.string.onboarding_pillar_maintenance_desc
        ),
        PillarItem(
            icon = Icons.Rounded.Description,
            tint = MaterialTheme.colorScheme.secondary,
            titleRes = R.string.onboarding_pillar_documents,
            descRes = R.string.onboarding_pillar_documents_desc
        ),
        PillarItem(
            icon = Icons.Rounded.NotificationsActive,
            tint = MaterialTheme.colorScheme.tertiary,
            titleRes = R.string.onboarding_pillar_reminders,
            descRes = R.string.onboarding_pillar_reminders_desc
        ),
        PillarItem(
            icon = Icons.AutoMirrored.Filled.FactCheck,
            tint = MaterialTheme.colorScheme.primary,
            titleRes = R.string.onboarding_pillar_ai,
            descRes = R.string.onboarding_pillar_ai_desc
        )
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // 16.dp card shape
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            pillars.forEachIndexed { index, pillar ->
                ValuePillarRow(
                    pillar = pillar,
                    modifier = Modifier.padding(horizontal = Dimensions.cardPadding, vertical = 14.dp)
                )
                if (index < pillars.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = Dimensions.cardPadding)
                    )
                }
            }
        }
    }
}

private data class PillarItem(
    val icon: ImageVector,
    val tint: Color,
    val titleRes: Int,
    val descRes: Int
)

@Composable
private fun ValuePillarRow(
    pillar: PillarItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = pillar.tint.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = pillar.icon,
                    contentDescription = null,
                    tint = pillar.tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(pillar.titleRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(pillar.descRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
