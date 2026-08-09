package com.autominder.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.ui.theme.Motion
import com.autominder.app.ui.util.icon
import com.autominder.app.ui.util.localizedLabel
import kotlin.math.floor
import kotlin.math.max

/** Gap between choices, and the width one choice needs before text wrapping ruins it. */
private val CHOICE_GAP = 8.dp
private val CHOICE_BASE_WIDTH = 84.dp
private const val MAX_COLUMNS = 4

/**
 * The service choice — one tap to say what was done, which is the single most
 * important interaction on Log Service.
 *
 * Column count is derived from available width against the width a choice
 * actually needs, and the required width scales with the user's font size. So
 * the grid reflows because a choice stopped fitting, not because a hardcoded
 * font-scale threshold was crossed — at 200% text it lands on one full-width
 * choice per row without any special case.
 *
 * Unselected choices carry `outline` (border/interactive, ≥3:1). A selectable
 * surface whose boundary is invisible is not identifiable as a control, which
 * is the one place WCAG 1.4.11 genuinely applies.
 *
 * Lives inside a verticalScroll parent, so it uses plain Rows — never a nested
 * LazyVerticalGrid.
 */
@Composable
fun ServiceTypeGrid(
    selected: ServiceType,
    onSelected: (ServiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val fontScale = LocalDensity.current.fontScale
    // A choice must grow with its label, or the text wraps into an unreadable stack.
    val requiredWidth = CHOICE_BASE_WIDTH * fontScale

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = max(
            1,
            floor((maxWidth + CHOICE_GAP) / (requiredWidth + CHOICE_GAP)).toInt()
        ).coerceAtMost(MAX_COLUMNS)

        Column(verticalArrangement = Arrangement.spacedBy(CHOICE_GAP)) {
            ServiceType.entries.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(CHOICE_GAP)) {
                    row.forEach { type ->
                        ServiceChoice(
                            type = type,
                            isSelected = type == selected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onSelected(type)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Keep the last row aligned with the ones above it.
                    repeat(columns - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
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
            .defaultMinSize(minHeight = 72.dp)
            .semantics {
                this.selected = isSelected
                role = Role.RadioButton
            },
        shape = MaterialTheme.shapes.medium,
        color = container,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = type.icon(),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = type.localizedLabel(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = contentColor
            )
        }
    }
}
