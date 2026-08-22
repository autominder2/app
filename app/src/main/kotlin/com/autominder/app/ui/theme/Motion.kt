package com.autominder.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring

/**
 * Shared motion vocabulary for the whole app.
 * Every screen pulls springs/easings/durations from here so the app
 * moves with one consistent physical character.
 */
object Motion {

    /**
     * Mirrors the system "Remove animations" accessibility setting
     * (Settings.Global.ANIMATOR_DURATION_SCALE == 0). Set once at startup in
     * MainActivity. When true, every spec here collapses to an instant snap and
     * callers drop large-amplitude motion (parallax drift, slide transitions),
     * which the accessibility guidance flags as a nausea trigger.
     *
     * Deliberately a plain @Volatile holder, not Compose state, so the
     * non-composable NavHost transition lambdas can read it too.
     */
    @Volatile
    var reduceMotion: Boolean = false

    /** Workhorse spring — list items, status morphs, card growth. */
    fun <T> springDefault(): FiniteAnimationSpec<T> =
        if (reduceMotion) snap()
        else spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)

    /** Softer spring for large surfaces (sheets, heroes, score arcs). */
    fun <T> springGentle(): FiniteAnimationSpec<T> =
        if (reduceMotion) snap()
        else spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)

    /** Snappy spring for small controls (chips, toggles, icons). */
    fun <T> springSnappy(): FiniteAnimationSpec<T> =
        if (reduceMotion) snap()
        else spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)

    /** M3 emphasized decelerate — entrances. */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** M3 emphasized accelerate — exits. */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    const val DurationShort = 200
    const val DurationMedium = 300
    const val DurationLong = 450

    /** Collapses to 0 when the user asked for no animations. */
    fun scaled(durationMillis: Int): Int = if (reduceMotion) 0 else durationMillis

    /** 0f when reduced, so callers can multiply out parallax/scale offsets. */
    val amplitude: Float get() = if (reduceMotion) 0f else 1f

    /** Per-index delay for staggered list/form entrances. */
    fun staggerDelay(index: Int): Int = if (reduceMotion) 0 else index * 40
}
