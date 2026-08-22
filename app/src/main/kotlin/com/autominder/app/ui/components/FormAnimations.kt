package com.autominder.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.autominder.app.ui.theme.Motion

/**
 * Staggered entrance wrapper for form fields: each field fades in and
 * slides up slightly, delayed by its index. Keeps the whole entrance
 * under ~300ms so the form never feels slow to use.
 */
@Composable
fun FormField(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember {
        androidx.compose.animation.core.MutableTransitionState(false).apply {
            targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = Motion.DurationShort,
                delayMillis = Motion.staggerDelay(index),
                easing = Motion.EmphasizedDecelerate
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = Motion.DurationShort,
                delayMillis = Motion.staggerDelay(index),
                easing = Motion.EmphasizedDecelerate
            ),
            initialOffsetY = { it / 6 }
        )
    ) {
        content()
    }
}
