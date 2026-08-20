package com.autominder.app.domain.model

import androidx.annotation.StringRes
import com.autominder.app.R

enum class QuoteVerdictStatus(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    LEGITIMATE_DUE(
        titleRes = R.string.quote_verdict_legitimate_title,
        descriptionRes = R.string.quote_verdict_legitimate_desc
    ),
    VERIFY_FIRST(
        titleRes = R.string.quote_verdict_verify_title,
        descriptionRes = R.string.quote_verdict_verify_desc
    ),
    LIKELY_UPSELL(
        titleRes = R.string.quote_verdict_upsell_title,
        descriptionRes = R.string.quote_verdict_upsell_desc
    ),
    CAN_WAIT(
        titleRes = R.string.quote_verdict_wait_title,
        descriptionRes = R.string.quote_verdict_wait_desc
    )
}

data class QuoteItem(
    val id: String,
    val serviceType: ServiceType,
    val customLabel: String? = null,
    val priceCents: Int = 0,
    val notes: String = ""
)

data class QuoteLineVerdict(
    val item: QuoteItem,
    val status: QuoteVerdictStatus,
    val reason: String,
    val questionToAsk: String,
    val fairPriceRangeCents: IntRange,
    val lastDoneOdometer: Int? = null,
    val lastDoneDate: Long? = null,
    val kmSinceLastDone: Int? = null
)

data class QuoteAuditResult(
    val vehicleName: String,
    val currentOdometer: Int,
    val lineVerdicts: List<QuoteLineVerdict>,
    val totalQuotedCents: Long,
    val fairPriceMinCents: Long,
    val fairPriceMaxCents: Long,
    val potentialSavingsCents: Long,
    val legitimateItemsCount: Int,
    val upsellItemsCount: Int,
    val verifyItemsCount: Int,
    val mechanicTalkingPoints: List<String>
)
