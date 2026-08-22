package com.autominder.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─── AutoMinder Shapes ──────────────────────────────────────────────────────
// Status-driven morphing: OVERDUE=8dp | DUE_SOON=16dp | GOOD=28dp
// M3 Expressive shape tokens for premium automotive feel.

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(40.dp)
)
