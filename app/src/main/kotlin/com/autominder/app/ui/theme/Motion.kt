package com.autominder.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Shared motion vocabulary for the whole app.
 * Every screen pulls springs/easings/durations from here so the app
 * moves with one consistent physical character.
 */
object Motion {

    /** Workhorse spring — list items, status morphs, card growth. */
    fun <T> springDefault() = spring<T>(
        dampingRatio = 0.7f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Softer spring for large surfaces (sheets, heroes, score arcs). */
    fun <T> springGentle() = spring<T>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessLow
    )

    /** Snappy spring for small controls (chips, toggles, icons). */
    fun <T> springSnappy() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** M3 emphasized decelerate — entrances. */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** M3 emphasized accelerate — exits. */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    const val DurationShort = 200
    const val DurationMedium = 300
    const val DurationLong = 450

    /** Per-index delay for staggered list/form entrances. */
    fun staggerDelay(index: Int): Int = index * 40
}
