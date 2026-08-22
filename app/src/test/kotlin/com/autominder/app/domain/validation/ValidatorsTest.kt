package com.autominder.app.domain.validation

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ValidatorsTest {

    @Test
    fun `valid year returns null`() {
        assertNull(Validators.validateYear(2020))
    }

    @Test
    fun `year before 1900 returns error`() {
        assertNotNull(Validators.validateYear(1899))
    }

    @Test
    fun `future year beyond next year returns error`() {
        val maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1
        assertNotNull(Validators.validateYear(maxYear + 1))
    }

    @Test
    fun `valid odometer returns null`() {
        assertNull(Validators.validateOdometer(50000))
    }

    @Test
    fun `zero odometer returns null`() {
        assertNull(Validators.validateOdometer(0))
    }

    @Test
    fun `negative odometer returns error`() {
        assertNotNull(Validators.validateOdometer(-1))
    }

    @Test
    fun `non-blank required field returns null`() {
        assertNull(Validators.validateRequired("Toyota", "Make"))
    }

    @Test
    fun `blank required field returns error`() {
        val result = Validators.validateRequired("", "Make")
        assertNotNull(result)
        assertEquals(ValidationErrorCode.FIELD_REQUIRED, result!!.code)
        assertTrue(result.args.contains("Make"))
    }

    @Test
    fun `valid VIN returns null`() {
        assertNull(Validators.validateVin("1HGBH41JXMN109186"))
    }

    @Test
    fun `empty VIN returns null`() {
        assertNull(Validators.validateVin(""))
    }

    @Test
    fun `short VIN returns error`() {
        assertNotNull(Validators.validateVin("1HGBH41"))
    }

    @Test
    fun `valid cost returns null`() {
        assertNull(Validators.validateCost(5000))
    }

    @Test
    fun `zero cost returns null`() {
        assertNull(Validators.validateCost(0))
    }

    @Test
    fun `negative cost returns error`() {
        assertNotNull(Validators.validateCost(-1))
    }
}
