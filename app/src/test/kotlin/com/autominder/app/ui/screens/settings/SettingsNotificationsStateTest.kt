package com.autominder.app.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards the notification switch against lying.
 *
 * Before 2026-08-26 the switch rendered `notificationsEnabled` — a stored
 * preference — with no reference to the OS permission. A user could grant
 * POST_NOTIFICATIONS, turn reminders on, later revoke the permission in Android
 * settings, and come back to a switch reading ON while Android silently dropped
 * every reminder. The app looked like it was working and was not.
 *
 * `BlockedBySystem` exists specifically so the UI can distinguish "the user
 * turned this off" from "Android is refusing", because only the second one
 * needs a route into system settings — a permission request will not re-prompt
 * once permanently denied.
 */
class SettingsNotificationsStateTest {

    private fun state(pref: Boolean, granted: Boolean) =
        SettingsUiState(notificationsEnabled = pref, hasNotificationPermission = granted)
            .notificationsState

    @Test
    fun `preference on and permission granted means reminders are active`() {
        assertEquals(NotificationsState.Active, state(pref = true, granted = true))
    }

    @Test
    fun `preference on but permission revoked is reported as blocked, never as active`() {
        assertEquals(
            "This is the regression: a revoked permission used to render as ON.",
            NotificationsState.BlockedBySystem,
            state(pref = true, granted = false)
        )
    }

    @Test
    fun `preference off is off regardless of permission`() {
        assertEquals(NotificationsState.Off, state(pref = false, granted = true))
        assertEquals(
            "The user's own choice outranks the permission — this is not a system block",
            NotificationsState.Off,
            state(pref = false, granted = false)
        )
    }

    @Test
    fun `only the active state should ever render the switch as on`() {
        val onStates = listOf(true to true, true to false, false to true, false to false)
            .map { (pref, granted) -> state(pref, granted) }
            .filter { it == NotificationsState.Active }

        assertEquals(
            "Exactly one of the four combinations may show reminders as running",
            1,
            onStates.size
        )
    }
}
