package com.autominder.app.domain.model

/**
 * How much the owner drives — the one onboarding answer that materially
 * changes the seeded plan's date axis. Stored/computed in km (domain law);
 * the UI renders miles-per-year equivalents for US users.
 */
enum class DrivingAmount(val annualKm: Int) {
    LOW(8_000),      // ≈ 5,000 mi/yr
    TYPICAL(16_000), // ≈ 10,000 mi/yr
    HIGH(28_000);    // ≈ 17,500 mi/yr

    companion object {
        fun fromNameOrDefault(name: String?): DrivingAmount =
            entries.firstOrNull { it.name == name } ?: TYPICAL
    }
}
