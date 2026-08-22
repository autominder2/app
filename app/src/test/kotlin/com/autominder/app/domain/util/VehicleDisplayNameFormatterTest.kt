package com.autominder.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleDisplayNameFormatterTest {

    @Test
    fun `standard vehicle makes and models format cleanly`() {
        assertEquals("Toyota RAV4", VehicleDisplayNameFormatter.format("Toyota", "RAV4"))
        assertEquals("Toyota Corolla", VehicleDisplayNameFormatter.format("Toyota", "Corolla"))
        assertEquals("Toyota Camry", VehicleDisplayNameFormatter.format("Toyota", "Camry"))
        assertEquals("Mercedes-Benz GLE 450", VehicleDisplayNameFormatter.format("Mercedes-Benz", "GLE 450"))
        assertEquals("Ford F-150", VehicleDisplayNameFormatter.format("Ford", "F-150"))
    }

    @Test
    fun `deduplicates make when repeated in model`() {
        assertEquals("Toyota RAV4", VehicleDisplayNameFormatter.format("Toyota", "Toyota RAV4"))
        assertEquals("Toyota Corolla", VehicleDisplayNameFormatter.format("Toyota", "toyota Corolla"))
        assertEquals("Ford F-150 Lightning", VehicleDisplayNameFormatter.format("Ford", "Ford F-150 Lightning"))
    }

    @Test
    fun `sanitizes noise tokens such as text or null or undefined`() {
        assertEquals("Toyota Camry", VehicleDisplayNameFormatter.format("Toyota", "text Camry"))
        assertEquals("Toyota Corolla", VehicleDisplayNameFormatter.format("Toyota text", "Corolla"))
        assertEquals("Honda Civic", VehicleDisplayNameFormatter.format("Honda", "Civic undefined"))
    }

    @Test
    fun `handles extraneous internal and surrounding whitespace`() {
        assertEquals("Ford F-150", VehicleDisplayNameFormatter.format("  Ford  ", "   F-150   "))
        assertEquals("BMW M3 Competition", VehicleDisplayNameFormatter.format("BMW", "M3    Competition"))
    }

    @Test
    fun `integrates year correctly when requested`() {
        assertEquals("2024 Toyota RAV4", VehicleDisplayNameFormatter.formatWithYear(2024, "Toyota", "RAV4"))
        assertEquals("2022 Honda Civic", VehicleDisplayNameFormatter.format("Honda", "Civic", year = 2022, includeYear = true))
        assertEquals("Honda Civic", VehicleDisplayNameFormatter.format("Honda", "Civic", year = 2022, includeYear = false))
    }

    @Test
    fun `handles empty and null makes gracefully`() {
        assertEquals("Civic", VehicleDisplayNameFormatter.format("", "Civic"))
        assertEquals("2022 Civic", VehicleDisplayNameFormatter.format(null, "Civic", year = 2022, includeYear = true))
        assertEquals("Toyota", VehicleDisplayNameFormatter.format("Toyota", null))
        assertEquals("2023 Toyota", VehicleDisplayNameFormatter.format("Toyota", "", year = 2023, includeYear = true))
    }

    @Test
    fun `handles completely empty or null inputs`() {
        assertEquals("Your car", VehicleDisplayNameFormatter.format(null, null))
        assertEquals("Your car", VehicleDisplayNameFormatter.format("", ""))
        assertEquals("2025 Vehicle", VehicleDisplayNameFormatter.format(null, null, year = 2025, includeYear = true))
    }
}
