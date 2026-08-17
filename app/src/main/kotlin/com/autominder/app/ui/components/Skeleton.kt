package com.autominder.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.autominder.app.ui.theme.Motion
import kotlinx.coroutines.delay

/**
 * Skeletons appear only after this delay. Room reads off a local database
 * usually resolve faster than this, and a skeleton that flashes for 40ms reads
 * as a glitch rather than as loading.
 */
private const val APPEARANCE_DELAY_MS = 150L

private const val PULSE_MIN_ALPHA = 0.40f
private const val PULSE_MAX_ALPHA = 0.70f
private const val PULSE_DURATION_MS = 1000

/** Held value under "Remove animations" — the midpoint of the pulse range. */
private const val STATIC_ALPHA = 0.55f

/**
 * The pulse is driven once per skeleton tree and shared by every cell, so a
 * screen full of placeholders costs one animation rather than one per bar, and
 * every cell stays in phase. Holds [State] rather than a raw Float so that
 * [skeletonFill] can read it during the draw phase — the value changes each
 * frame, and reading it in composition would recompose every cell instead of
 * merely redrawing it.
 */
private val LocalSkeletonAlpha = compositionLocalOf<State<Float>> {
    mutableFloatStateOf(STATIC_ALPHA)
}

/**
 * Gates appearance on [APPEARANCE_DELAY_MS] and provides the shared pulse.
 *
 * The animation is an opacity pulse, not a translating gradient sweep: a moving
 * gradient repaints the full bounds of every cell each frame for no
 * informational gain, and nothing about a loading list is actually travelling
 * left to right (`DESIGN_SYSTEM_2026 §9`).
 */
@Composable
private fun SkeletonScaffold(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(APPEARANCE_DELAY_MS)
        visible = true
    }
    if (!visible) return

    val alpha: State<Float> = if (Motion.reduceMotion) {
        remember { mutableFloatStateOf(STATIC_ALPHA) }
    } else {
        rememberInfiniteTransition(label = "skeletonPulse").animateFloat(
            initialValue = PULSE_MIN_ALPHA,
            targetValue = PULSE_MAX_ALPHA,
            animationSpec = infiniteRepeatable(
                animation = tween(PULSE_DURATION_MS, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "skeletonAlpha"
        )
    }

    CompositionLocalProvider(LocalSkeletonAlpha provides alpha) { content() }
}

/**
 * Fill for a single skeleton cell. Outside a [SkeletonScaffold] this renders as
 * a static tint rather than animating on its own.
 */
private fun Modifier.skeletonFill(shape: Shape = RoundedCornerShape(8.dp)): Modifier = composed {
    val color = MaterialTheme.colorScheme.surfaceVariant
    val alpha = LocalSkeletonAlpha.current
    clip(shape).drawBehind { drawRect(color = color, alpha = alpha.value) }
}

/** A single skeleton bar — the primitive cell. */
@Composable
fun SkeletonBar(
    modifier: Modifier = Modifier,
    heightDp: Int = 16,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    Box(modifier = modifier.height(heightDp.dp).skeletonFill(shape))
}

/** Dashboard first-paint: health placeholder + a few vehicle cards. */
@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    SkeletonScaffold {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(140.dp).skeletonFill(CircleShape))
            Spacer(modifier = Modifier.height(4.dp))
            repeat(3) {
                VehicleCardSkeleton()
            }
        }
    }
}

@Composable
fun VehicleCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).skeletonFill(CircleShape))
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
    SkeletonScaffold {
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
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
}
