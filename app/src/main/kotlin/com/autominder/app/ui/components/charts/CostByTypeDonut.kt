package com.autominder.app.ui.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.ui.screens.vehicle.TypeSpend
import com.autominder.app.ui.screens.vehicle.VehicleDetailViewModel
import com.autominder.app.ui.theme.Motion

/**
 * Donut of all-time spend per service type with a sweep-in animation and
 * a plain-composable legend (label + amount per slice).
 */
@Composable
fun CostByTypeDonut(
    data: List<TypeSpend>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val total = data.sumOf { it.cents }.coerceAtLeast(1)

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = Motion.springGentle(),
        label = "donut_sweep"
    )

    val sliceColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.outline
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(110.dp)) {
                val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Butt)
                val inset = stroke.width / 2
                var startAngle = -90f
                data.forEachIndexed { index, slice ->
                    val sweep = 360f * (slice.cents.toFloat() / total) * progress
                    drawArc(
                        color = sliceColors[index % sliceColors.size],
                        startAngle = startAngle,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0f), // 2° gap between slices
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - stroke.width,
                            size.height - stroke.width
                        ),
                        style = stroke
                    )
                    startAngle += 360f * (slice.cents.toFloat() / total) * progress
                }
            }
            Text(
                text = VehicleDetailViewModel.formatCost(total),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            data.forEachIndexed { index, slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = sliceColors[index % sliceColors.size],
                        shape = CircleShape,
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = VehicleDetailViewModel.formatCost(slice.cents),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
