package com.autominder.app.domain.util

object VehicleDisplayName {

    /**
     * Normalizes and formats vehicle make and model into a clean, human-readable display string.
     * Prevents duplicate make prefixes, accidental garbled concatenations, and blank values.
     */
    fun format(make: String?, model: String?, year: Int? = null): String {
        val cleanMake = make?.trim().orEmpty()
        val cleanModel = model?.trim().orEmpty()

        if (cleanMake.isBlank() && cleanModel.isBlank()) {
            return if (year != null && year > 1900) "$year Vehicle" else "Your car"
        }

        if (cleanMake.isBlank()) {
            return if (year != null && year > 1900) "$year $cleanModel" else cleanModel
        }

        if (cleanModel.isBlank()) {
            return if (year != null && year > 1900) "$year $cleanMake" else cleanMake
        }

        // If model starts with make (e.g. make="Toyota", model="Toyota RAV4"), avoid repeating make
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

        return baseName.trim()
    }
}
