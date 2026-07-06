package com.autominder.app.ui.components.premium

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.ui.theme.Motion

/**
 * Single source of truth for status-driven presentation in the premium kit:
 * the brand corner morph (OVERDUE=8dp sharp → GOOD=28dp soft), semantic
 * container/content colors, and the accent rail. Every premium component
 * delegates here instead of re-mapping status to style locally.
 *
 * Status text/chips are NOT handled here — reuse the existing
 * [com.autominder.app.ui.components.StatusChip] for that.
 */
object PremiumStatusStyle {

    /** Brand shape language: urgency sharpens corners. */
    fun cornerRadius(status: ServiceStatus): Dp = when (status) {
        ServiceStatus.OVERDUE -> 8.dp
        ServiceStatus.DUE_SOON -> 16.dp
        else -> 28.dp
    }

    /** Corner morph animated through the shared motion vocabulary. */
    @Composable
    fun animatedCornerShape(status: ServiceStatus, label: String = "statusCorner"): Shape {
        val radius by animateDpAsState(
            targetValue = cornerRadius(status),
            animationSpec = Motion.springDefault(),
            label = label
        )
        return RoundedCornerShape(radius)
    }

    @Composable
    fun containerColor(status: ServiceStatus): Color = when (status) {
        ServiceStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
        ServiceStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiaryContainer
        ServiceStatus.SNOOZED -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    @Composable
    fun contentColor(status: ServiceStatus): Color = when (status) {
        ServiceStatus.OVERDUE -> MaterialTheme.colorScheme.onErrorContainer
        ServiceStatus.DUE_SOON -> MaterialTheme.colorScheme.onTertiaryContainer
        ServiceStatus.SNOOZED -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    /**
     * Left accent rail color — the non-color-blind-hostile second signal.
     * Null means no rail (calm states don't shout).
     */
    @Composable
    fun railColor(status: ServiceStatus): Color? = when (status) {
        ServiceStatus.OVERDUE -> MaterialTheme.colorScheme.error
        ServiceStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiary
        else -> null
    }

    val RailWidth: Dp = 4.dp
}
