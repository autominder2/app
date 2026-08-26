package com.autominder.app.ui.components

import androidx.annotation.StringRes
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.paywall_trial_reassurance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
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

/**
 * One row of the free-vs-Pro table.
 *
 * [proOnly] must be backed by a real entitlement check in code. Advertising a
 * capability as Pro-exclusive when a free user already receives it is a refund
 * generator and a Play misrepresentation risk, so [gateEvidence] records where
 * the gate lives and `ProPaywallTruthTest` asserts the two stay in sync.
 */
internal data class PaywallFeature(
    @StringRes val labelRes: Int,
    val proOnly: Boolean,
    val gateEvidence: String?
)

/**
 * The ONLY two entitlement gates that exist in v1.0, verified 2026-08-25:
 *  - ads removed          -> MainActivity.kt:274  `if (!isProUser) BannerAdView(...)`
 *  - cost + efficiency    -> VehicleDetailScreen.kt:681 and :765 `ProFeatureGate(...)`
 *
 * Everything else is reachable by a free user, so it is listed as available to
 * both tiers rather than dangled behind the paywall. Do not flip a row to
 * proOnly without adding the gate first — adding gates is a feature change and
 * is out of scope during the v1.0 freeze.
 */
internal val PAYWALL_FEATURES: List<PaywallFeature> = listOf(
    PaywallFeature(R.string.paywall_feature_reminders, proOnly = false, gateEvidence = null),
    PaywallFeature(R.string.paywall_feature_fuel_log, proOnly = false, gateEvidence = null),
    PaywallFeature(R.string.paywall_feature_garage, proOnly = false, gateEvidence = null),
    // Reachable free from the dashboard banner (DashboardScreen.kt:220) via an
    // ungated route (NavGraph.kt:262) — was advertised as Pro-only.
    PaywallFeature(R.string.paywall_feature_quote_auditor, proOnly = false, gateEvidence = null),
    // CSV export, free from Records (ServiceHistoryViewModel.kt:99). Never PDF,
    // nothing certifies it — was advertised as "Certified Vehicle Passport (PDF & CSV)".
    PaywallFeature(R.string.paywall_feature_passport, proOnly = false, gateEvidence = null),
    // PredictDueUseCase feeds ungated vehicle detail — was advertised as Pro-only.
    PaywallFeature(R.string.paywall_feature_predictions, proOnly = false, gateEvidence = null),
    PaywallFeature(
        R.string.paywall_feature_cost_analytics,
        proOnly = true,
        gateEvidence = "VehicleDetailScreen.kt:681,765"
    ),
    PaywallFeature(
        R.string.paywall_feature_ad_free,
        proOnly = true,
        gateEvidence = "MainActivity.kt:274"
    )
)

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
            PAYWALL_FEATURES.forEach { feature ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                FeatureRow(
                    feature = stringResource(feature.labelRes),
                    free = !feature.proOnly,
                    pro = true
                )
            }
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
