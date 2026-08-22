package com.autominder.app.ui.screens.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autominder.app.R
import com.autominder.app.domain.model.DrivingAmount
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.Dimensions
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.util.DistanceFormat

@Composable
fun DrivingAmountChips(
    selected: DrivingAmount,
    onSelected: (DrivingAmount) -> Unit,
    distanceUnit: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.itemGapSmall)
    ) {
        DrivingAmount.entries.forEach { amount ->
            val isSelected = selected == amount
            val config = getDrivingAmountConfig(amount, distanceUnit)

            Surface(
                shape = RoundedCornerShape(14.dp), // field/row shape convention
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onSelected(amount)
                        },
                        role = Role.RadioButton
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Icon
                    Surface(
                        shape = CircleShape,
                        color = (if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = config.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Title & Range Subtitle
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(config.titleRes),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = config.rangeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Annualized Mono Value + Radio Indicator
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.driving_support_value,
                                DistanceFormat.grouped(
                                    DistanceUtil.kmToDisplay(amount.annualKm, distanceUnit)
                                ),
                                DistanceUtil.unitLabel(distanceUnit)
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class DrivingConfig(
    val icon: ImageVector,
    val titleRes: Int,
    val rangeText: String
)

@Composable
private fun getDrivingAmountConfig(amount: DrivingAmount, distanceUnit: String): DrivingConfig {
    val unit = DistanceUtil.unitLabel(distanceUnit)
    return when (amount) {
        DrivingAmount.LOW -> {
            val maxDisplay = DistanceFormat.grouped(DistanceUtil.kmToDisplay(8_000, distanceUnit))
            DrivingConfig(
                icon = Icons.Rounded.Eco,
                titleRes = R.string.driving_low,
                rangeText = stringResource(R.string.driving_range_low, maxDisplay, unit)
            )
        }
        DrivingAmount.TYPICAL -> {
            val minDisplay = DistanceFormat.grouped(DistanceUtil.kmToDisplay(8_000, distanceUnit))
            val maxDisplay = DistanceFormat.grouped(DistanceUtil.kmToDisplay(20_000, distanceUnit))
            DrivingConfig(
                icon = Icons.Rounded.DirectionsCar,
                titleRes = R.string.driving_typical,
                rangeText = stringResource(R.string.driving_range_typical, minDisplay, maxDisplay, unit)
            )
        }
        DrivingAmount.HIGH -> {
            val minDisplay = DistanceFormat.grouped(DistanceUtil.kmToDisplay(20_000, distanceUnit))
            DrivingConfig(
                icon = Icons.Rounded.Speed,
                titleRes = R.string.driving_high,
                rangeText = stringResource(R.string.driving_range_high, minDisplay, unit)
            )
        }
    }
}
