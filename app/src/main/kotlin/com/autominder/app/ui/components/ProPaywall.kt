package com.autominder.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autominder.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPaywall(
    sheetState: SheetState,
    // Product-details-driven prices — null while Play's product query is
    // still loading. Never hardcoded, never assumed.
    monthlyPrice: String?,
    yearlyPrice: String?,
    lifetimePrice: String?,
    onDismiss: () -> Unit,
    onSelectMonthly: () -> Unit,
    onSelectYearly: () -> Unit,
    onSelectLifetime: () -> Unit,
    onRestorePurchases: () -> Unit
) {
    val loadingPriceText = stringResource(R.string.paywall_price_loading)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.paywall_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            FeatureComparisonTable()

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSelectYearly,
                enabled = yearlyPrice != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.paywall_yearly_label, yearlyPrice ?: loadingPriceText),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.paywall_yearly_best_value),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSelectMonthly,
                enabled = monthlyPrice != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.paywall_monthly_label, monthlyPrice ?: loadingPriceText))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onSelectLifetime,
                enabled = lifetimePrice != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.paywall_lifetime_label, lifetimePrice ?: loadingPriceText))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onRestorePurchases) {
                Text(
                    text = stringResource(R.string.paywall_restore),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureComparisonTable() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // No vehicle-count gate exists in v1.0 — both tiers are unlimited.
            FeatureRow(stringResource(R.string.paywall_feature_vehicles), free = true, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_reminders), free = true, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_fuel_log), free = true, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_analytics), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_charts), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_backup), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_export), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_ad_free), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_widgets), free = false, pro = true)
        }
    }
}

@Composable
private fun FeatureRow(feature: String, free: Boolean, pro: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        FeatureCheck(enabled = free, modifier = Modifier.weight(0.3f))
        FeatureCheck(enabled = pro, modifier = Modifier.weight(0.3f))
    }
}

@Composable
private fun FeatureRow(feature: String, free: String, pro: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = free,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = pro,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.3f)
        )
    }
}

@Composable
private fun FeatureCheck(enabled: Boolean, modifier: Modifier = Modifier) {
    Icon(
        imageVector = if (enabled) Icons.Default.Check else Icons.Default.Close,
        contentDescription = null,
        modifier = modifier.size(18.dp),
        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    )
}
