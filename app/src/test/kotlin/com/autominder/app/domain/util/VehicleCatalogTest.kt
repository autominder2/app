package com.autominder.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleCatalogTest {

    @Test
    fun `searchMakes with blank query returns all makes`() {
        val result = VehicleCatalog.searchMakes("  ")
        assertEquals(VehicleCatalog.allMakes.size, result.size)
        assertEquals(VehicleCatalog.allMakes, result)
    }

    @Test
    fun `searchMakes ranks prefix matches before contains matches`() {
        val result = VehicleCatalog.searchMakes("to")
        assertTrue(result.isNotEmpty())
        assertEquals("Toyota", result.first())
        assertTrue(result.contains("Aston Martin"))
    }

    @Test
    fun `searchMakes is case-insensitive and trims whitespace`() {
        val lower = VehicleCatalog.searchMakes("  toyota  ")
        val upper = VehicleCatalog.searchMakes("TOYOTA")
        assertEquals(listOf("Toyota"), lower)
        assertEquals(listOf("Toyota"), upper)
    }

    @Test
    fun `searchMakes with no matches returns empty list without throwing`() {
        val result = VehicleCatalog.searchMakes("NonExistentMakeXYZ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `modelsForMake returns known models for make regardless of case and spacing`() {
        val models = VehicleCatalog.modelsForMake("  toyota  ")
        assertTrue(models.contains("RAV4"))
        assertTrue(models.contains("Camry"))
        assertTrue(models.contains("Corolla"))
    }

    @Test
    fun `modelsForMake returns empty list for unknown make`() {
        val models = VehicleCatalog.modelsForMake("CustomKitCar")
        assertTrue(models.isEmpty())
    }

    @Test
    fun `searchModels filters models within a make`() {
        val result = VehicleCatalog.searchModels("Toyota", "co")
        assertTrue(result.contains("Corolla"))
        assertFalse(result.contains("RAV4"))
    }

    @Test
    fun `searchModels with blank query returns all models for make`() {
        val full = VehicleCatalog.modelsForMake("Ford")
        val search = VehicleCatalog.searchModels("Ford", "  ")
        assertEquals(full, search)
    }

    @Test
    fun `data integrity - no duplicate makes in allMakes case-insensitively`() {
        val lowerMakes = VehicleCatalog.allMakes.map { it.lowercase() }
        assertEquals(
            "allMakes contains duplicate make names",
            lowerMakes.size,
            lowerMakes.toSet().size
        )
    }

    @Test
    fun `regression guard - allMakes is a superset of legacy brands`() {
        val legacyBrands = listOf(
            "Toyota", "Honda", "Ford", "Chevrolet", "Nissan", "Hyundai", "Kia",
            "Volkswagen", "BMW", "Tesla", "Mazda", "Subaru", "Jeep"
        )
        for (brand in legacyBrands) {
            assertTrue(
                "Legacy brand '$brand' missing from allMakes",
                VehicleCatalog.allMakes.any { it.equals(brand, ignoreCase = true) }
            )
        }
    }
}
