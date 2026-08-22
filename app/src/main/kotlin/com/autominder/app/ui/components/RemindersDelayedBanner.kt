package com.autominder.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.ui.util.DateFormatUtil

/**
 * Tells the user that background checks have stopped arriving.
 *
 * Three deliberate restraints:
 *
 * **No status colour.** This is a system notice, not a vehicle state. Painting
 * it in the caution amber reserved for `DUE_SOON` would put it in competition
 * with real reminders and dilute a six-value status vocabulary that the whole
 * UI depends on. `surfaceContainerHigh` reads as *quieter* than any vehicle
 * alert, which is exactly its rank.
 *
 * **No animation.** An infinite opacity pulse was considered and rejected. It
 * would violate the "no infinite animation without genuine ongoing activity"
 * rule for a fact that is entirely static; text oscillating between 0.40 and
 * 0.70 alpha fails WCAG 1.4.3 contrast for most of its cycle, so the warning
 * would be least readable at the moment it matters; and holding the frame
 * pipeline awake indefinitely burns the battery of a user whose complaint is
 * that the system is already throttling this app. Entry motion is left to the
 * host list's `animateItem()`.
 *
 * **A real destination.** The action opens battery optimization settings,
 * which is the actual fix. A banner that only describes a problem is a
 * dead-end CTA.
 */
@Composable
fun RemindersDelayedBanner(
    lastCheckedAt: Long?,
    onFixClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val body = if (lastCheckedAt != null) {
        stringResource(R.string.reminders_delayed_body, DateFormatUtil.formatDate(lastCheckedAt))
    } else {
        stringResource(R.string.reminders_delayed_body_never)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            // Merged so TalkBack reads the notice as one statement instead of
            // three fragments, with the action still focusable on its own.
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                // Decorative: the title beside it already carries the meaning.
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            // The action sits below the text rather than beside it so the
            // layout still holds at 2.0x font scale and in RTL.
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.reminders_delayed_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onFixClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                ) {
                    Text(stringResource(R.string.reminders_delayed_action))
                }
            }
        }
    }
}
