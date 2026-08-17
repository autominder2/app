package com.autominder.app.ui.components.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.ui.components.StatusChip

/**
 * The maintenance verdict: a plain sentence the owner can act on, such as
 * "2 maintenance items need attention" or "Nothing due right now".
 *
 * Deliberately has no score, ring, gauge or grade. AutoMinder only knows what
 * its owner typed in — it reads no telemetry and performs no diagnosis — so
 * any 0-100 figure here would be a number the app invented. The previous
 * implementation did exactly that, and worse, treated UNKNOWN as a perfect
 * 100, meaning a vehicle the app knew nothing about scored identically to one
 * that was fully maintained.
 *
 * Status is conveyed by [StatusChip] (icon + label + colour) plus the wording
 * of [headlineText] — never by colour alone, and never by the card's shape.
 * Card shape is fixed by component family, not by status.
 *
 * Presentational only: no ViewModel, repository or navigation access.
 */
@Composable
fun MaintenanceVerdictCard(
    headlineText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    status: ServiceStatus? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        // One TalkBack stop for the whole verdict: chip, headline and
        // supporting line are read together as a single sentence.
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (status != null) {
                StatusChip(status = status)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = headlineText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 0.dp, vertical = 0.dp
                    )
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
