package com.autominder.app.domain.validation

import java.util.Calendar

/**
 * Pure Kotlin identifier for a validation failure — carries no display text.
 * Callers (ViewModels) map [code] to a localized string resource and supply
 * any runtime values via [args] for placeholder substitution.
 */
data class ValidationError(
    val code: ValidationErrorCode,
    val args: List<Any> = emptyList()
)

enum class ValidationErrorCode {
    YEAR_TOO_EARLY,
    YEAR_TOO_LATE,
    ODOMETER_NEGATIVE,
    FIELD_REQUIRED,
    VIN_INVALID_FORMAT,
    COST_NEGATIVE
}

object Validators {

    /**
     * Validates that a year falls within a reasonable range for vehicles.
     * @return a [ValidationError] or null if valid
     */
    fun validateYear(year: Int): ValidationError? {
        val maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1
        return when {
            year < 1900 -> ValidationError(ValidationErrorCode.YEAR_TOO_EARLY)
            year > maxYear -> ValidationError(ValidationErrorCode.YEAR_TOO_LATE, listOf(maxYear))
            else -> null
        }
    }

    /**
     * Validates that an odometer reading is non-negative.
     * @return a [ValidationError] or null if valid
     */
    fun validateOdometer(odometer: Int): ValidationError? {
        return if (odometer < 0) {
            ValidationError(ValidationErrorCode.ODOMETER_NEGATIVE)
        } else {
            null
        }
    }

    /**
     * Validates that a required string field is not blank.
     * @return a [ValidationError] or null if valid
     */
    fun validateRequired(value: String, fieldName: String): ValidationError? {
        return if (value.isBlank()) {
            ValidationError(ValidationErrorCode.FIELD_REQUIRED, listOf(fieldName))
        } else {
            null
        }
    }

    /**
     * Validates a Vehicle Identification Number (VIN).
     * If the VIN is blank or null, it is considered valid (optional field).
     * If provided, it must be exactly 17 alphanumeric characters.
     * @return a [ValidationError] or null if valid
     */
    fun validateVin(vin: String): ValidationError? {
        if (vin.isBlank()) return null
        val alphanumericRegex = Regex("^[A-Za-z0-9]{17}$")
        return if (!alphanumericRegex.matches(vin)) {
            ValidationError(ValidationErrorCode.VIN_INVALID_FORMAT)
        } else {
            null
        }
    }

    /**
     * Validates that a cost in cents is non-negative.
     * @return a [ValidationError] or null if valid
     */
    fun validateCost(costCents: Int): ValidationError? {
        return if (costCents < 0) {
            ValidationError(ValidationErrorCode.COST_NEGATIVE)
        } else {
            null
        }
    }
}
