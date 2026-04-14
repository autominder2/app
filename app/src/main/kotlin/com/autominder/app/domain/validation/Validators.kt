package com.autominder.app.domain.validation

import java.util.Calendar

object Validators {

    /**
     * Validates that a year falls within a reasonable range for vehicles.
     * @return error message or null if valid
     */
    fun validateYear(year: Int): String? {
        val maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1
        return when {
            year < 1900 -> "Year must be 1900 or later"
            year > maxYear -> "Year cannot be later than $maxYear"
            else -> null
        }
    }

    /**
     * Validates that an odometer reading is non-negative.
     * @return error message or null if valid
     */
    fun validateOdometer(odometer: Int): String? {
        return if (odometer < 0) {
            "Odometer reading cannot be negative"
        } else {
            null
        }
    }

    /**
     * Validates that a required string field is not blank.
     * @return error message or null if valid
     */
    fun validateRequired(value: String, fieldName: String): String? {
        return if (value.isBlank()) {
            "$fieldName is required"
        } else {
            null
        }
    }

    /**
     * Validates a Vehicle Identification Number (VIN).
     * If the VIN is blank or null, it is considered valid (optional field).
     * If provided, it must be exactly 17 alphanumeric characters.
     * @return error message or null if valid
     */
    fun validateVin(vin: String): String? {
        if (vin.isBlank()) return null
        val alphanumericRegex = Regex("^[A-Za-z0-9]{17}$")
        return if (!alphanumericRegex.matches(vin)) {
            "VIN must be exactly 17 alphanumeric characters"
        } else {
            null
        }
    }

    /**
     * Validates that a cost in cents is non-negative.
     * @return error message or null if valid
     */
    fun validateCost(costCents: Int): String? {
        return if (costCents < 0) {
            "Cost cannot be negative"
        } else {
            null
        }
    }
}
