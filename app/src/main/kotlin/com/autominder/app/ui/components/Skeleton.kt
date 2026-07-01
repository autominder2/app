package com.autominder.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.autominder.app.ui.theme.Motion

/**
 * Animated shimmer fill. Structured skeletons beat a blank spinner: the first
 * paint already shows the shape of the content, which reads as fast + premium.
 * Falls back to a static tint when the user has "Remove animations" on.
 */
fun Modifier.shimmer(shape: Shape = RoundedCornerShape(8.dp)): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    if (Motion.reduceMotion) {
        return@composed clip(shape).background(base)
    }
    val highlight = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    val width = size.width.toFloat().coerceAtLeast(1f)
    val startX = -width + progress * 2f * width
    clip(shape)
        .onSizeChanged { size = it }
        .background(
            Brush.linearGradient(
                colors = listOf(base, highlight, base),
                start = Offset(startX, 0f),
                end = Offset(startX + width, 0f)
            )
        )
}

/** A single shimmering bar — the primitive skeleton cell. */
@Composable
fun SkeletonBar(
    modifier: Modifier = Modifier,
    heightDp: Int = 16,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    Box(modifier = modifier.height(heightDp.dp).shimmer(shape))
}

/** Dashboard first-paint: health placeholder + a few vehicle cards. */
@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Health score ring placeholder
        Box(modifier = Modifier.size(140.dp).shimmer(CircleShape))
        Spacer(modifier = Modifier.height(4.dp))
        repeat(3) {
            VehicleCardSkeleton()
        }
    }
}

@Composable
fun VehicleCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).shimmer(CircleShape))
            Spacer(modifier = Modifier.padding(start = 12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBar(modifier = Modifier.fillMaxWidth(0.5f), heightDp = 18)
                SkeletonBar(modifier = Modifier.fillMaxWidth(0.3f), heightDp = 12)
            }
        }
        SkeletonBar(modifier = Modifier.fillMaxWidth(), heightDp = 40, shape = RoundedCornerShape(12.dp))
    }
}

/** Generic list first-paint used by history / fuel / mileage screens. */
@Composable
fun ListSkeleton(modifier: Modifier = Modifier, rows: Int = 6) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(rows) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeletonBar(modifier = Modifier.fillMaxWidth(0.45f), heightDp = 18)
                    SkeletonBar(modifier = Modifier.fillMaxWidth(0.2f), heightDp = 18)
                }
                SkeletonBar(modifier = Modifier.fillMaxWidth(0.3f), heightDp = 12)
            }
        }
    }
}
