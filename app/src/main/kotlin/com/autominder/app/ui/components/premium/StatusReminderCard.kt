package com.autominder.app.ui.components.premium

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.ui.components.StatusChip
import com.autominder.app.ui.theme.Motion

/**
 * The premium replacement for undifferentiated "red slab" reminder rows.
 * Status speaks four ways — StatusChip text, semantic container, animated
 * corner morph (8/16/28dp via [PremiumStatusStyle]), and accent rail.
 * Timing lines arrive pre-localized and pre-formatted from the caller
 * (overdue-by-mileage first, demoted date second — the Slice 1A contract).
 *
 * Action labels are caller-supplied strings so this component contains
 * zero hardcoded copy.
 */
@Composable
fun StatusReminderCard(
    title: String,
    status: ServiceStatus,
    timingPrimary: String,
    modifier: Modifier = Modifier,
    timingSecondary: String? = null,
    doneLabel: String? = null,
    snoozeLabel: String? = null,
    editContentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    onSnooze: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val shape = PremiumStatusStyle.animatedCornerShape(status, label = "reminderCorner")
    val containerColor by animateColorAsState(
        targetValue = PremiumStatusStyle.containerColor(status),
        animationSpec = Motion.springDefault(),
        label = "reminderContainer"
    )
    val contentColor = PremiumStatusStyle.contentColor(status)
    val rail = PremiumStatusStyle.railColor(status)

    ElevatedCard(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (status == ServiceStatus.OVERDUE) 3.dp else 1.dp
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            if (rail != null) {
                Box(
                    modifier = Modifier
                        .width(PremiumStatusStyle.RailWidth)
                        .fillMaxHeight()
                        .background(rail)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (onEdit != null) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = editContentDescription,
                                modifier = Modifier.size(18.dp),
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                    StatusChip(status = status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = timingPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                if (timingSecondary != null) {
                    Text(
                        text = timingSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }

                if ((doneLabel != null && onDone != null) || (snoozeLabel != null && onSnooze != null)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        // Start, not End. A FAB permanently owns the bottom-end
                        // corner of the screen, so a right-aligned row action
                        // collides with it at whatever scroll offset brings the
                        // two together — on VehicleDetail the FAB sat directly
                        // on top of "Done". Trailing content padding cannot fix
                        // that: it only clears the LAST item, while a FAB covers
                        // whatever happens to be beneath it. Leading alignment is
                        // immune at every scroll position, and it puts the
                        // actions under the text they act on. Alignment.Start
                        // also mirrors correctly in RTL, which End did too but
                        // into the same collision.
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (snoozeLabel != null && onSnooze != null) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    onSnooze()
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .minimumInteractiveComponentSize(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Snooze,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = contentColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(snoozeLabel, color = contentColor, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        if (doneLabel != null && onDone != null) {
                            FilledTonalButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                    onDone()
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .minimumInteractiveComponentSize(),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = doneLabel,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
