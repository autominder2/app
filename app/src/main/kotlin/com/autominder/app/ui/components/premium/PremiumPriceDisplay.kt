package com.autominder.app.ui.components.premium

/**
 * Closed price state for paywall plan surfaces. Play's ProductDetails query
 * is asynchronous and can fail outright (no Play services, network down),
 * so "no price yet" and "no price ever" are distinct states the UI must
 * render differently — a nullable String can't say which.
 *
 * Display text for [Loading]/[Unavailable] is supplied by the caller from
 * string resources; this type carries state, never copy.
 */
sealed interface PremiumPriceDisplay {
    /** Query in flight — render a quiet placeholder, keep the card disabled. */
    data object Loading : PremiumPriceDisplay

    /** Play returned a localized, formatted price string. */
    data class Available(val text: String) : PremiumPriceDisplay

    /** Query failed or billing unsupported — say so, don't spin forever. */
    data object Unavailable : PremiumPriceDisplay
}
