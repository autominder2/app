package com.autominder.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleDisplayNameTest {

    @Test
    fun `format combines make and model cleanly`() {
        val result = VehicleDisplayName.format("Toyota", "RAV4")
        assertEquals("Toyota RAV4", result)
    }

    @Test
    fun `format removes duplicate make prefix if present in model`() {
        val result = VehicleDisplayName.format("Toyota", "Toyota RAV4")
        assertEquals("Toyota RAV4", result)
    }

    @Test
    fun `format handles case insensitivity in duplicate make prefix`() {
        val result = VehicleDisplayName.format("Toyota", "toyota Corolla")
        assertEquals("Toyota Corolla", result)
    }

    @Test
    fun `format handles blank make by falling back to model`() {
        val result = VehicleDisplayName.format("", "Civic", 2022)
        assertEquals("2022 Civic", result)
    }

    @Test
    fun `format handles blank model by falling back to make`() {
        val result = VehicleDisplayName.format("Honda", "", 2023)
        assertEquals("2023 Honda", result)
    }

    @Test
    fun `format handles completely empty values with fallback`() {
        val result = VehicleDisplayName.format(null, null)
        assertEquals("Your car", result)
    }

    @Test
    fun `format trims surrounding whitespaces`() {
        val result = VehicleDisplayName.format("  Ford  ", "  F-150 Lightning  ")
        assertEquals("Ford F-150 Lightning", result)
    }
}
