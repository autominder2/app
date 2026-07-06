package com.autominder.app.ui.components.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.ui.theme.JetBrainsMono

/**
 * Fleet-style bold metric moment: eyebrow label, JetBrains Mono value,
 * optional unit and supporting line. Values arrive pre-formatted
 * (DistanceFormat.grouped / cost formatting at the caller) — this card
 * never computes or formats.
 */
@Composable
fun InsightMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    supportingText: String? = null,
    icon: ImageVector? = null,
    emphasized: Boolean = false
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.medium,
        color = if (emphasized) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        val contentColor = if (emphasized) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val secondaryColor = if (emphasized) {
            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = secondaryColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryColor
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMono),
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMono),
                        color = secondaryColor,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor
                )
            }
        }
    }
}

/** Two metrics side by side — the standard cockpit arrangement. */
@Composable
fun InsightMetricRow(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}
