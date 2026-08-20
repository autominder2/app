package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class PredictDueUseCaseTest {

    private lateinit var useCase: PredictDueUseCase
    private val now = 1_700_000_000_000L // Fixed baseline timestamp
    private val dayMillis = TimeUnit.DAYS.toMillis(1)

    @Before
    fun setUp() {
        useCase = PredictDueUseCase()
    }

    @Test
    fun `dailyKmRate returns null when fewer than 2 points`() {
        assertNull(useCase.dailyKmRate(emptyList()))
        assertNull(useCase.dailyKmRate(listOf(OdometerPoint(10000, now))))
    }

    @Test
    fun `dailyKmRate returns null when observation span is less than 3 days`() {
        val points = listOf(
            OdometerPoint(10000, now),
            OdometerPoint(10100, now + dayMillis * 2) // only 2 days
        )
        assertNull(useCase.dailyKmRate(points))
    }

    @Test
    fun `dailyKmRate returns null when km driven is zero or negative`() {
        val points = listOf(
            OdometerPoint(10000, now),
            OdometerPoint(9900, now + dayMillis * 10) // negative jump
        )
        assertNull(useCase.dailyKmRate(points))
    }

    @Test
    fun `dailyKmRate calculates correct rate over valid multi-day span`() {
        // 300 km driven over 10 days = 30.0 km/day
        val points = listOf(
            OdometerPoint(10000, now),
            OdometerPoint(10150, now + dayMillis * 5),
            OdometerPoint(10300, now + dayMillis * 10)
        )
        val rate = useCase.dailyKmRate(points)
        assertNotNull(rate)
        assertEquals(30.0, rate!!, 0.001)
    }

    @Test
    fun `predict projects date from km trigger when daily rate is present`() {
        val reminder = Reminder(
            id = 1L,
            vehicleId = 5L,
            serviceType = ServiceType.OIL_CHANGE,
            nextDueOdometer = 10500, // 500 km away from 10000
            nextDueDate = null
        )

        // 500 km remaining at 50 km/day = 10 days from now
        val prediction = useCase.predict(
            reminder = reminder,
            currentOdometerKm = 10000,
            dailyKmRate = 50.0,
            nowMillis = now
        )

        assertEquals(500, prediction.kmRemaining)
        val expectedDate = now + (10 * dayMillis)
        assertEquals(expectedDate, prediction.predictedAt)
    }

    @Test
    fun `predict picks earlier date between km projection and explicit date trigger`() {
        val reminder = Reminder(
            id = 1L,
            vehicleId = 5L,
            serviceType = ServiceType.BRAKE_SERVICE,
            nextDueOdometer = 10200, // 200 km away -> 4 days at 50 km/day
            nextDueDate = now + (14 * dayMillis) // 14 days away
        )

        val prediction = useCase.predict(
            reminder = reminder,
            currentOdometerKm = 10000,
            dailyKmRate = 50.0,
            nowMillis = now
        )

        // Km projected (4 days) is earlier than date trigger (14 days)
        val expectedKmDate = now + (4 * dayMillis)
        assertEquals(expectedKmDate, prediction.predictedAt)
    }

    @Test
    fun `predict without rate falls back to date trigger`() {
        val dateTrigger = now + (30 * dayMillis)
        val reminder = Reminder(
            id = 1L,
            vehicleId = 5L,
            serviceType = ServiceType.REGISTRATION,
            nextDueOdometer = null,
            nextDueDate = dateTrigger
        )

        val prediction = useCase.predict(
            reminder = reminder,
            currentOdometerKm = 10000,
            dailyKmRate = null,
            nowMillis = now
        )

        assertNull(prediction.kmRemaining)
        assertEquals(dateTrigger, prediction.predictedAt)
    }
}
