package com.autominder.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.autominder.app.ui.theme.Motion

/**
 * Springs the element down to [pressedScale] while held for a tactile "give"
 * on touch — the small deformation premium apps use on cards and buttons.
 * Pass the same [interactionSource] you give to clickable(). No-ops under
 * reduced motion.
 */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !Motion.reduceMotion) pressedScale else 1f,
        animationSpec = Motion.springSnappy(),
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
