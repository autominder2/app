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
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autominder.app.R
import com.autominder.app.ui.components.premium.PremiumPaywallPlanCard
import com.autominder.app.ui.components.premium.PremiumPriceDisplay

private enum class PaywallPlan { MONTHLY, YEARLY, LIFETIME }

/**
 * Conversion-first paywall: selectable plan cards (radio semantics via
 * [PremiumPaywallPlanCard]) with a single Continue CTA, instead of three
 * stacked buttons competing for attention. Yearly is pre-selected as the
 * anchor plan. Prices arrive nullable from Play's product query — null
 * renders the card's Loading state and disables selection/Continue.
 */
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
    var selectedPlan by rememberSaveable { mutableStateOf(PaywallPlan.YEARLY) }
    val loadingLabel = stringResource(R.string.paywall_price_loading)
    val unavailableLabel = stringResource(R.string.paywall_price_unavailable)

    fun display(price: String?): PremiumPriceDisplay =
        if (price != null) PremiumPriceDisplay.Available(price) else PremiumPriceDisplay.Loading

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

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PremiumPaywallPlanCard(
                    periodLabel = stringResource(R.string.paywall_plan_yearly),
                    price = display(yearlyPrice),
                    loadingLabel = loadingLabel,
                    unavailableLabel = unavailableLabel,
                    selected = selectedPlan == PaywallPlan.YEARLY,
                    onClick = { selectedPlan = PaywallPlan.YEARLY },
                    badgeLabel = stringResource(R.string.paywall_yearly_best_value)
                )
                PremiumPaywallPlanCard(
                    periodLabel = stringResource(R.string.paywall_plan_monthly),
                    price = display(monthlyPrice),
                    loadingLabel = loadingLabel,
                    unavailableLabel = unavailableLabel,
                    selected = selectedPlan == PaywallPlan.MONTHLY,
                    onClick = { selectedPlan = PaywallPlan.MONTHLY }
                )
                PremiumPaywallPlanCard(
                    periodLabel = stringResource(R.string.paywall_plan_lifetime),
                    price = display(lifetimePrice),
                    loadingLabel = loadingLabel,
                    unavailableLabel = unavailableLabel,
                    selected = selectedPlan == PaywallPlan.LIFETIME,
                    onClick = { selectedPlan = PaywallPlan.LIFETIME },
                    subtitle = stringResource(R.string.paywall_plan_lifetime_subtitle)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val continueEnabled = when (selectedPlan) {
                PaywallPlan.MONTHLY -> monthlyPrice != null
                PaywallPlan.YEARLY -> yearlyPrice != null
                PaywallPlan.LIFETIME -> lifetimePrice != null
            }
            Button(
                onClick = {
                    when (selectedPlan) {
                        PaywallPlan.MONTHLY -> onSelectMonthly()
                        PaywallPlan.YEARLY -> onSelectYearly()
                        PaywallPlan.LIFETIME -> onSelectLifetime()
                    }
                },
                enabled = continueEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.paywall_continue),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.paywall_column_free),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.3f)
                )
                Text(
                    text = stringResource(R.string.paywall_column_pro),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.3f)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_reminders), free = true, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_fuel_log), free = true, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_quote_auditor), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_garage), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_passport), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_predictions), free = false, pro = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FeatureRow(stringResource(R.string.paywall_feature_ad_free), free = false, pro = true)
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
private fun FeatureCheck(enabled: Boolean, modifier: Modifier = Modifier) {
    Icon(
        imageVector = if (enabled) Icons.Default.Check else Icons.Default.Close,
        contentDescription = stringResource(
            if (enabled) R.string.cd_paywall_included else R.string.cd_paywall_not_included
        ),
        modifier = modifier.size(18.dp),
        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    )
}
