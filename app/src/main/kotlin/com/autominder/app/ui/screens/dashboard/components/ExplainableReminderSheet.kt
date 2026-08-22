package com.autominder.app.ui.screens.dashboard.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.domain.usecase.DataConfidence
import com.autominder.app.domain.usecase.ReminderExplanation
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.theme.Exo2
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplainableReminderSheet(
    explanation: ReminderExplanation,
    distanceUnit: String,
    onDismiss: () -> Unit,
    onLogService: () -> Unit,
    modifier: Modifier = Modifier,
    onEditReminder: () -> Unit = onDismiss
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Icon + Title + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.why_reminder_title),
                        fontFamily = Exo2,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (explanation.isSafetyCritical) {
                            "${explanation.serviceTitle} • Safety-related"
                        } else {
                            explanation.serviceTitle
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Clean Breakdown Table (Unboxed)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Next due
                val targetDateStr = explanation.targetDueDate?.let { DateFormatUtil.formatDate(it) }
                val targetOdoStr = explanation.targetDueOdometer?.let {
                    val displayKm = DistanceUtil.kmToDisplay(it, distanceUnit)
                    "${DistanceFormat.grouped(displayKm)} ${DistanceUtil.unitLabel(distanceUnit)}"
                }
                val dueDisplay = when {
                    targetDateStr != null && targetOdoStr != null -> "$targetDateStr or $targetOdoStr"
                    targetDateStr != null -> targetDateStr
                    targetOdoStr != null -> targetOdoStr
                    else -> "Pending odometer update"
                }
                ProofRow(
                    label = "Next due",
                    value = dueDisplay,
                    isHighlighted = true
                )

                // Service rule
                ProofRow(
                    label = "Service rule",
                    value = "${explanation.ruleDescription} (${stringResource(R.string.why_reminder_rule_desc)})"
                )

                // Current mileage
                val currentOdoDisplay = DistanceUtil.kmToDisplay(explanation.currentOdometer, distanceUnit)
                ProofRow(
                    label = "Current mileage",
                    value = "${DistanceFormat.grouped(currentOdoDisplay)} ${DistanceUtil.unitLabel(distanceUnit)}"
                )

                // Last recorded service
                val lastServiceStr = if (explanation.lastServiceOdometer != null && explanation.lastServiceDate != null) {
                    val lastKm = DistanceUtil.kmToDisplay(explanation.lastServiceOdometer, distanceUnit)
                    "${DistanceFormat.grouped(lastKm)} ${DistanceUtil.unitLabel(distanceUnit)} • ${DateFormatUtil.formatDate(explanation.lastServiceDate)}"
                } else {
                    stringResource(R.string.why_reminder_no_previous_service)
                }
                ProofRow(
                    label = "Last service",
                    value = lastServiceStr
                )
            }

            // Confidence / Data Quality Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (explanation.confidence) {
                    DataConfidence.HIGH -> Color(0xFFECFDF5)
                    DataConfidence.MEDIUM -> MaterialTheme.colorScheme.surfaceContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = when (explanation.confidence) {
                            DataConfidence.HIGH -> Color(0xFF10B981)
                            DataConfidence.MEDIUM -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (explanation.confidence) {
                            DataConfidence.HIGH -> stringResource(R.string.why_reminder_mileage_current)
                            DataConfidence.MEDIUM -> "Reminder based on recent mileage update"
                            else -> "Estimate • Update mileage for precision"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when (explanation.confidence) {
                            DataConfidence.HIGH -> Color(0xFF065F46)
                            DataConfidence.MEDIUM -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLogService,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Build,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.why_reminder_log_completed),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(
                    onClick = onEditReminder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.why_reminder_edit),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProofRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isHighlighted) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
