package com.autominder.app.ui.components.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.ui.components.StatusChip

/**
 * "What to do next" card for the Dashboard cockpit: service title, a one-line
 * human reason (already localized — e.g. the overdue-by-mileage phrasing),
 * and exactly one CTA. Status is communicated four ways: StatusChip text,
 * semantic container color, corner morph, and accent rail — never color alone.
 */
@Composable
fun ProactiveAttentionCard(
    title: String,
    reasonText: String,
    status: ServiceStatus,
    modifier: Modifier = Modifier,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = PremiumStatusStyle.animatedCornerShape(status, label = "attentionCorner")
    val container = PremiumStatusStyle.containerColor(status)
    val content = PremiumStatusStyle.contentColor(status)
    val rail = PremiumStatusStyle.railColor(status)

    ElevatedCard(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = container,
            disabledContainerColor = container
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (status == ServiceStatus.OVERDUE) 3.dp else 1.dp
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            if (rail != null) {
                Box(
                    modifier = Modifier
                        .width(PremiumStatusStyle.RailWidth)
                        .fillMaxHeight()
                        .background(rail)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = content,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(status = status)
                }
                Text(
                    text = reasonText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content.copy(alpha = 0.9f)
                )
                if (ctaLabel != null && onCta != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCta) {
                            Text(
                                text = ctaLabel,
                                color = content,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
