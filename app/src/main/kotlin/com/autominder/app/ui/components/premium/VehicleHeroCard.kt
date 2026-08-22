package com.autominder.app.ui.components.premium

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.autominder.app.ui.components.pressScale
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.VehicleBodyType
import com.autominder.app.ui.theme.JetBrainsMono
import com.autominder.app.ui.util.toDrawableRes

/** Layout variants: [Compact] for list rows, [Expanded] for detail heroes. */
enum class VehicleHeroVariant { Compact, Expanded }

/**
 * The car as an emotional object, not a database row: photo (or a branded
 * icon tile fallback), Exo 2 identity, JetBrains Mono odometer, and a status
 * slot. All display strings arrive pre-formatted and pre-localized — pass
 * odometer through DistanceFormat.grouped() at the caller, pass yearText
 * as null when the year is unknown (never "0").
 */
@Composable
fun VehicleHeroCard(
    title: String,
    modifier: Modifier = Modifier,
    variant: VehicleHeroVariant = VehicleHeroVariant.Compact,
    yearText: String? = null,
    odometerText: String? = null,
    photoUri: String? = null,
    photoContentDescription: String? = null,
    mergedContentDescription: String? = null,
    statusChip: (@Composable () -> Unit)? = null,
    /** Precision Rail — status accent on the leading edge, resolved through
     *  [PremiumStatusStyle.railColor] so calm states render no rail at all.
     *  Purely decorative (status is still carried by [statusChip] + text);
     *  null (the default) renders no rail, preserving prior call sites. */
    railStatus: ServiceStatus? = null,
    /** One-line "next maintenance concern" (e.g. "Oil change is overdue"),
     *  already fully formatted and localized by the caller. Null renders
     *  nothing, preserving prior call sites. */
    concernText: String? = null,
    /** Semantic vehicle body type silhouette to render when [photoUri] is null. */
    bodyType: com.autominder.app.domain.model.VehicleBodyType = com.autominder.app.domain.model.VehicleBodyType.DEFAULT,
    onClick: (() -> Unit)? = null
) {
    val semanticsModifier = if (mergedContentDescription != null) {
        Modifier.semantics(mergeDescendants = true) {
            contentDescription = mergedContentDescription
        }
    } else {
        Modifier.semantics(mergeDescendants = true) {}
    }

    // Single source of truth for the rail: PremiumStatusStyle. Null for calm
    // states — the rail only appears when a status genuinely needs attention.
    val rail = railStatus?.let { PremiumStatusStyle.railColor(it) }
    val concernColor = rail ?: MaterialTheme.colorScheme.onSurfaceVariant

    val cardContent: @Composable () -> Unit = {
        val content: @Composable () -> Unit = {
            when (variant) {
                VehicleHeroVariant.Compact -> CompactContent(
                    title, yearText, odometerText, photoUri, photoContentDescription,
                    statusChip, concernText, concernColor, bodyType
                )
                VehicleHeroVariant.Expanded -> ExpandedContent(
                    title, yearText, odometerText, photoUri, photoContentDescription,
                    statusChip, concernText, concernColor, bodyType
                )
            }
        }
        if (rail != null) {
            // Same structure as StatusReminderCard: IntrinsicSize.Min bounds
            // the Row's height so the rail's fillMaxHeight() resolves (inside
            // a LazyColumn item the incoming max height is infinite, which
            // collapses a bare fillMaxHeight() to zero). The Card's own
            // Surface clips the rail's square corners to the card shape.
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(PremiumStatusStyle.RailWidth)
                        .fillMaxHeight()
                        .background(rail)
                )
                Box(modifier = Modifier.weight(1f)) { content() }
            }
        } else {
            content()
        }
    }

    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        ElevatedCard(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = modifier
                .fillMaxWidth()
                .pressScale(interactionSource)
                .then(semanticsModifier),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) { cardContent() }
    } else {
        ElevatedCard(
            modifier = modifier.fillMaxWidth().then(semanticsModifier),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) { cardContent() }
    }
}

@Composable
private fun CompactContent(
    title: String,
    yearText: String?,
    odometerText: String?,
    photoUri: String?,
    photoContentDescription: String?,
    statusChip: (@Composable () -> Unit)?,
    concernText: String? = null,
    concernColor: Color = Color.Unspecified,
    bodyType: com.autominder.app.domain.model.VehicleBodyType = com.autominder.app.domain.model.VehicleBodyType.DEFAULT
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VehicleAvatar(photoUri, photoContentDescription, size = 56.dp, bodyType = bodyType)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (yearText != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = yearText,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (odometerText != null) {
                Text(
                    text = odometerText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (concernText != null) {
                Text(
                    text = concernText,
                    style = MaterialTheme.typography.bodySmall,
                    color = concernColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        statusChip?.invoke()
    }
}

@Composable
private fun ExpandedContent(
    title: String,
    yearText: String?,
    odometerText: String?,
    photoUri: String?,
    photoContentDescription: String?,
    statusChip: (@Composable () -> Unit)?,
    concernText: String? = null,
    concernColor: Color = Color.Unspecified,
    bodyType: com.autominder.app.domain.model.VehicleBodyType = com.autominder.app.domain.model.VehicleBodyType.DEFAULT
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (photoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Shimmer placeholder shown while the image is loading.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(photoUri)
                        .crossfade(400)
                        .build(),
                    contentDescription = photoContentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0f),
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f)
                                ),
                                startY = 100f
                            )
                        )
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (photoUri == null) {
                VehicleAvatar(photoUri = null, photoContentDescription, size = 64.dp, bodyType = bodyType)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val metaLine = listOfNotNull(yearText, odometerText).joinToString(" · ")
                if (metaLine.isNotEmpty()) {
                    Text(
                        text = metaLine,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (concernText != null) {
                    Text(
                        text = concernText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = concernColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            statusChip?.invoke()
        }
    }
}

@Composable
private fun VehicleAvatar(
    photoUri: String?,
    photoContentDescription: String?,
    size: androidx.compose.ui.unit.Dp,
    bodyType: com.autominder.app.domain.model.VehicleBodyType = com.autominder.app.domain.model.VehicleBodyType.DEFAULT
) {
    if (photoUri != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photoUri)
                .crossfade(true)
                .build(),
            contentDescription = photoContentDescription,
            modifier = Modifier
                .size(size)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(bodyType.toDrawableRes()),
                contentDescription = photoContentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size / 5),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
