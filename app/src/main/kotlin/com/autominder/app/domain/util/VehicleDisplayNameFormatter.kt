package com.autominder.app.domain.util

/**
 * Canonical vehicle display name formatter.
 *
 * Provides deterministic, clean, and deduplicated vehicle title representations
 * across the entire application (Home, Garage, Vehicle Detail, Activity, Reminders).
 *
 * Rules:
 * • Make/Model deduplication (e.g., make="Toyota", model="Toyota RAV4" -> "Toyota RAV4")
 * • Extraneous whitespace & noise normalization (e.g., "Toyota   text   Camry" -> "Toyota Camry")
 * • Year integration (e.g., "2022 Toyota RAV4")
 * • Graceful fallback for empty or missing values ("Your car", "2022 Vehicle")
 */
object VehicleDisplayNameFormatter {

    private val NOISE_PATTERNS = listOf(
        Regex("(?i)\\btext\\b"),
        Regex("(?i)\\bundefined\\b"),
        Regex("(?i)\\bnull\\b")
    )

    /**
     * Formats make and model into a canonical display string.
     *
     * @param make Vehicle manufacturer (e.g. "Toyota", "Mercedes-Benz")
     * @param model Vehicle model (e.g. "RAV4", "GLE 450", "F-150")
     * @param year Optional manufacture year (e.g. 2022)
     * @param includeYear If true, prefixes the valid year (e.g. "2022 Toyota RAV4")
     */
    fun format(
        make: String?,
        model: String?,
        year: Int? = null,
        includeYear: Boolean = false
    ): String {
        val cleanMake = sanitizeInput(make)
        val cleanModel = sanitizeInput(model)
        val hasValidYear = year != null && year in 1900..2100

        if (cleanMake.isBlank() && cleanModel.isBlank()) {
            return if (hasValidYear && includeYear) "$year Vehicle" else "Your car"
        }

        if (cleanMake.isBlank()) {
            return if (hasValidYear && includeYear) "$year $cleanModel" else cleanModel
        }

        if (cleanModel.isBlank()) {
            return if (hasValidYear && includeYear) "$year $cleanMake" else cleanMake
        }

        // Deduplicate make if model already starts with the make name
        val resolvedModel = if (cleanModel.startsWith(cleanMake, ignoreCase = true)) {
            cleanModel.substring(cleanMake.length).trim()
        } else {
            cleanModel
        }

        val baseName = if (resolvedModel.isNotBlank()) {
            "$cleanMake $resolvedModel"
        } else {
            cleanMake
        }

        return if (hasValidYear && includeYear) {
            "$year $baseName"
        } else {
            baseName
        }
    }

    /**
     * Formats with year included if available (e.g. "2024 Toyota RAV4").
     */
    fun formatWithYear(year: Int?, make: String?, model: String?): String {
        return format(make = make, model = model, year = year, includeYear = true)
    }

    /**
     * Sanitizes raw text: removes noise tokens, collapses multiple spaces, trims.
     */
    private fun sanitizeInput(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var text = input.trim()
        for (pattern in NOISE_PATTERNS) {
            text = pattern.replace(text, "")
        }
        // Collapse internal multiple whitespaces into a single space
        return text.replace(Regex("\\s+"), " ").trim()
    }
}
