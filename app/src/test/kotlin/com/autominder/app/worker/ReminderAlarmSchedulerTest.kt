package com.autominder.app.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Covers the roll-to-tomorrow boundary of the daily backstop alarm.
 *
 * `setAndAllowWhileIdle` is one-shot, so a trigger time computed in the past
 * fires immediately and then the chain re-arms into the past again — a tight
 * loop that would drain a battery in the name of saving one. The strictly-in-
 * the-future guarantee is the whole safety of this design.
 */
class ReminderAlarmSchedulerTest {

    private fun localTime(hour: Int, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 10)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun hourOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

    private fun dayOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_MONTH)

    @Test
    fun `before the check hour, the alarm lands today`() {
        val now = localTime(hour = 6)
        val trigger = ReminderAlarmScheduler.nextTriggerMillis(now)

        assertEquals(9, hourOf(trigger))
        assertEquals(dayOf(now), dayOf(trigger))
    }

    @Test
    fun `after the check hour, the alarm rolls to tomorrow`() {
        val now = localTime(hour = 14)
        val trigger = ReminderAlarmScheduler.nextTriggerMillis(now)

        assertEquals(9, hourOf(trigger))
        assertEquals(dayOf(now) + 1, dayOf(trigger))
    }

    /**
     * Exactly on the hour must roll forward, not schedule for the instant
     * that has already arrived.
     */
    @Test
    fun `exactly at the check hour rolls to tomorrow`() {
        val now = localTime(hour = 9)
        val trigger = ReminderAlarmScheduler.nextTriggerMillis(now)

        assertEquals(dayOf(now) + 1, dayOf(trigger))
    }

    @Test
    fun `the trigger is always strictly in the future`() {
        for (hour in 0..23) {
            val now = localTime(hour = hour, minute = 30)
            val trigger = ReminderAlarmScheduler.nextTriggerMillis(now)
            assertTrue("hour $hour produced a trigger at or before now", trigger > now)
        }
    }
}
