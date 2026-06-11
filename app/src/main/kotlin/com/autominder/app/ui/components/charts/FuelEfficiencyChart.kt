package com.autominder.app.ui.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.autominder.app.ui.theme.Motion
import java.util.Locale

/**
 * km/L trend line across fill-ups. The line reveals left-to-right on
 * first display; min/max bounds shown as plain text labels.
 */
@Composable
fun FuelEfficiencyChart(
    series: List<Double>,
    modifier: Modifier = Modifier
) {
    if (series.size < 2) return

    val minValue = series.min()
    val maxValue = series.max()
    val range = (maxValue - minValue).takeIf { it > 0.0001 } ?: 1.0

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = Motion.springGentle(),
        label = "line_reveal"
    )

    val lineColor = MaterialTheme.colorScheme.tertiary
    val dotColor = MaterialTheme.colorScheme.onTertiaryContainer
    val gridColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(vertical = 4.dp)
        ) {
            val stepX = size.width / (series.size - 1)
            val points = series.mapIndexed { index, value ->
                Offset(
                    x = index * stepX,
                    y = size.height * (1f - ((value - minValue) / range).toFloat() * 0.8f - 0.1f)
                )
            }

            listOf(0.1f, 0.9f).forEach { fraction ->
                drawLine(
                    color = gridColor,
                    start = Offset(0f, size.height * fraction),
                    end = Offset(size.width, size.height * fraction),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Reveal the polyline left-to-right with the spring progress
            val visibleWidth = size.width * progress
            val path = Path()
            points.forEachIndexed { index, point ->
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            clipRect(right = visibleWidth) {
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
                points.forEach { point ->
                    drawCircle(color = dotColor, radius = 3.dp.toPx(), center = point)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%.1f", minValue),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = String.format(Locale.getDefault(), "%.1f km/L", maxValue),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
