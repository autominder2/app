package com.autominder.app.ui.components.premium

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.ui.components.StatusChip
import com.autominder.app.ui.theme.Motion

/**
 * The cockpit verdict: a human sentence leads ("7 services need attention"),
 * the score ring is demoted to a supporting instrument. Never renders a
 * lone giant number — the headline is always scalable [Text] supplied by
 * the caller, already localized.
 *
 * @param score 0-100 for the instrument ring, or null to hide it entirely.
 * @param scoreDescription accessibility text for the ring (e.g. "Health
 *   score 35 out of 100") — required when [score] is non-null.
 */
@Composable
fun HealthCockpitCard(
    headlineText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    status: ServiceStatus? = null,
    score: Int? = null,
    scoreDescription: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                if (score != null && scoreDescription != null) {
                    contentDescription = "$headlineText. $scoreDescription"
                }
            },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (status != null) {
                    StatusChip(status = status)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (actionLabel != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 0.dp, vertical = 0.dp
                        )
                    ) {
                        Text(actionLabel)
                    }
                }
            }

            if (score != null) {
                ScoreInstrument(score = score.coerceIn(0, 100), status = status)
            }
        }
    }
}

/**
 * The demoted instrument: a small arc with the numeric score as real,
 * font-scalable [Text] centered inside. Decorative for TalkBack — the
 * merged card semantics already announce the score.
 */
@Composable
private fun ScoreInstrument(score: Int, status: ServiceStatus?) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = Motion.springGentle(),
        label = "cockpitScore"
    )
    val arcColor = when (status) {
        ServiceStatus.OVERDUE -> MaterialTheme.colorScheme.error
        ServiceStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val sweepMax = 270f
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = sweepMax,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = arcColor,
                startAngle = 135f,
                sweepAngle = sweepMax * (animatedScore / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = animatedScore.toInt().toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = arcColor
        )
    }
}
