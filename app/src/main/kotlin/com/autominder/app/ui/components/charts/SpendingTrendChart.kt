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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autominder.app.ui.screens.vehicle.MonthlySpend
import com.autominder.app.ui.theme.Motion

/**
 * Six-month spending bar chart. Bars grow in with a spring on first
 * display; month labels are regular composables under the canvas so we
 * never draw text manually.
 */
@Composable
fun SpendingTrendChart(
    data: List<MonthlySpend>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val maxCents = (data.maxOfOrNull { it.cents } ?: 0).coerceAtLeast(1)

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = Motion.springGentle(),
        label = "bar_growth"
    )

    val barColor = MaterialTheme.colorScheme.primary
    val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val barCount = data.size
            val slotWidth = size.width / barCount
            val barWidth = slotWidth * 0.55f

            // Light horizontal guides at 50% and 100%
            listOf(0f, 0.5f).forEach { fraction ->
                val y = size.height * fraction
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            data.forEachIndexed { index, month ->
                val fraction = month.cents.toFloat() / maxCents
                val barHeight = (size.height * fraction * progress).coerceAtLeast(
                    if (month.cents > 0) 4.dp.toPx() else 2.dp.toPx()
                )
                val left = index * slotWidth + (slotWidth - barWidth) / 2
                drawRoundRect(
                    color = if (month.cents > 0) barColor else emptyBarColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { month ->
                Text(
                    text = month.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
