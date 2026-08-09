package com.autominder.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.ui.theme.Motion
import com.autominder.app.ui.util.icon
import com.autominder.app.ui.util.localizedLabel

/**
 * The services most owners actually log, in rough order of how often a private
 * car needs them. Deliberately hand-picked and fixed — a ranking model would be
 * unpredictable, and this list is short enough to be right by inspection.
 */
private val COMMON_SERVICES = listOf(
    ServiceType.OIL_CHANGE,
    ServiceType.TIRE_ROTATION,
    ServiceType.BRAKE_SERVICE,
    ServiceType.AIR_FILTER,
    ServiceType.BATTERY,
    ServiceType.INSPECTION
)

/**
 * Roughly how many choices the first viewport can carry before it stops being a
 * shortcut. Recent history claims these slots first — a third genuinely recent
 * service beats a generic common one, so the split is never a fixed 2 + 4.
 */
private const val FAST_PATH_CAPACITY = 6
private const val MIN_VISIBLE_COMMON = 2

/**
 * "What was done?" — the single most important interaction on Log Service.
 *
 * The full taxonomy is sixteen entries. Showing all sixteen up front pushes the
 * odometer below the fold and turns a fifteen-second task into a scan, so the
 * screen opens with what this owner actually uses: their own recent history for
 * this vehicle first, then a short common set, with the complete list one tap
 * away. The wall is still reachable; it is just no longer the front door.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ServiceChoicePicker(
    selected: ServiceType,
    recentTypes: List<ServiceType>,
    onSelected: (ServiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAll by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Anything already offered as "recently used" would be noise if repeated,
    // and the current selection must always be visible even from the long list.
    val common = remember(recentTypes, selected) {
        val remaining = (FAST_PATH_CAPACITY - recentTypes.size).coerceAtLeast(MIN_VISIBLE_COMMON)
        (COMMON_SERVICES + selected)
            .distinct()
            .filterNot { it in recentTypes }
            .take(remaining)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (recentTypes.isNotEmpty()) {
            SectionLabel(stringResource(R.string.add_service_recently_used))
            ChoiceFlow(
                types = recentTypes,
                selected = selected,
                onSelected = onSelected
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        SectionLabel(stringResource(R.string.add_service_common))
        ChoiceFlow(
            types = common,
            selected = selected,
            onSelected = onSelected
        )

        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                showAll = true
            },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_service_view_all),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showAll) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAll = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Text(
                text = stringResource(R.string.add_service_all_services),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(ServiceType.entries, key = { it.name }) { type ->
                    AllServicesRow(
                        type = type,
                        isSelected = type == selected,
                        onClick = {
                            onSelected(type)
                            showAll = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        // Section labels are structure, not action. Cobalt is reserved for
        // selection, primary actions and focus — if every heading is blue,
        // blue stops meaning anything.
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/**
 * Wraps choices by available width. At 200% text a choice needs more room, so
 * fewer fit per line and the flow becomes a single column on its own — no
 * font-scale branch anywhere.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceFlow(
    types: List<ServiceType>,
    selected: ServiceType,
    onSelected: (ServiceType) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        types.forEach { type ->
            ServiceChoice(
                type = type,
                isSelected = type == selected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onSelected(type)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxRowHeight()
            )
        }
    }
}

@Composable
private fun ServiceChoice(
    type: ServiceType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = Motion.springSnappy(),
        label = "choice_container"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = Motion.springSnappy(),
        label = "choice_border"
    )
    // Border weight is the non-colour channel — selection survives greyscale.
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = Motion.springSnappy(),
        label = "choice_border_width"
    )
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .semantics {
                this.selected = isSelected
                role = Role.RadioButton
            },
        shape = MaterialTheme.shapes.medium,
        color = container,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = type.icon(),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = type.localizedLabel(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Composable
private fun AllServicesRow(
    type: ServiceType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                selected = isSelected
                role = Role.RadioButton
            },
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = type.icon(),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = type.localizedLabel(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Start,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
