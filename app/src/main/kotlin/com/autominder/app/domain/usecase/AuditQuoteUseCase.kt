package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.QuoteAuditResult
import com.autominder.app.domain.model.QuoteItem
import com.autominder.app.domain.model.QuoteLineVerdict
import com.autominder.app.domain.model.QuoteVerdictStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.suggestedInterval
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AuditQuoteUseCase @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val serviceRepository: IServiceRepository
) {

    suspend operator fun invoke(
        vehicleId: Long,
        items: List<QuoteItem>
    ): QuoteAuditResult? {
        val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull() ?: return null
        val serviceHistory = serviceRepository.getServicesForVehicle(vehicleId).firstOrNull() ?: emptyList()

        val lineVerdicts = items.map { item ->
            auditLineItem(item, vehicle.currentOdometer, serviceHistory)
        }

        val totalQuoted = items.sumOf { it.priceCents.toLong() }
        val fairMin = lineVerdicts.sumOf { it.fairPriceRangeCents.first.toLong() }
        val fairMax = lineVerdicts.sumOf { it.fairPriceRangeCents.last.toLong() }

        val upsellCents = lineVerdicts
            .filter { it.status == QuoteVerdictStatus.LIKELY_UPSELL || it.status == QuoteVerdictStatus.CAN_WAIT }
            .sumOf { it.item.priceCents.toLong() }

        val legitCount = lineVerdicts.count { it.status == QuoteVerdictStatus.LEGITIMATE_DUE }
        val upsellCount = lineVerdicts.count { it.status == QuoteVerdictStatus.LIKELY_UPSELL }
        val verifyCount = lineVerdicts.count { it.status == QuoteVerdictStatus.VERIFY_FIRST || it.status == QuoteVerdictStatus.CAN_WAIT }

        val talkingPoints = buildTalkingPoints(lineVerdicts)

        return QuoteAuditResult(
            vehicleName = "${vehicle.make} ${vehicle.model}",
            currentOdometer = vehicle.currentOdometer,
            lineVerdicts = lineVerdicts,
            totalQuotedCents = totalQuoted,
            fairPriceMinCents = fairMin,
            fairPriceMaxCents = fairMax,
            potentialSavingsCents = upsellCents,
            legitimateItemsCount = legitCount,
            upsellItemsCount = upsellCount,
            verifyItemsCount = verifyCount,
            mechanicTalkingPoints = talkingPoints
        )
    }

    private fun auditLineItem(
        item: QuoteItem,
        currentOdometer: Int,
        history: List<com.autominder.app.domain.model.Service>
    ): QuoteLineVerdict {
        val lastService = history
            .filter { it.serviceType == item.serviceType }
            .maxByOrNull { it.serviceDate }

        val suggestedKm = item.serviceType.suggestedInterval().km ?: 15_000
        val fairRange = getFairPriceRange(item.serviceType)

        if (lastService == null) {
            // First time seeing this service on this vehicle
            val status = if (currentOdometer >= suggestedKm) {
                QuoteVerdictStatus.LEGITIMATE_DUE
            } else if (currentOdometer >= suggestedKm * 0.7) {
                QuoteVerdictStatus.VERIFY_FIRST
            } else {
                QuoteVerdictStatus.CAN_WAIT
            }

            val reason = when (status) {
                QuoteVerdictStatus.LEGITIMATE_DUE -> "No prior record logged and current mileage (${currentOdometer} km) exceeds typical ${suggestedKm} km interval."
                QuoteVerdictStatus.VERIFY_FIRST -> "Approaching recommended ${suggestedKm} km interval. Check physical wear before approving."
                else -> "Vehicle mileage (${currentOdometer} km) is well below the standard ${suggestedKm} km interval for this service."
            }

            val question = getQuestionFor(item.serviceType, status, null)

            return QuoteLineVerdict(
                item = item,
                status = status,
                reason = reason,
                questionToAsk = question,
                fairPriceRangeCents = fairRange,
                lastDoneOdometer = null,
                lastDoneDate = null,
                kmSinceLastDone = null
            )
        }

        val kmSince = currentOdometer - lastService.odometerAtService

        val status: QuoteVerdictStatus
        val reason: String

        if (kmSince < (suggestedKm * 0.45)) {
            // Premature upsell!
            status = QuoteVerdictStatus.LIKELY_UPSELL
            reason = "Replaced only ${kmSince} km ago at ${lastService.odometerAtService} km. Typical interval is ${suggestedKm} km. Premature replacement."
        } else if (kmSince < (suggestedKm * 0.8)) {
            status = QuoteVerdictStatus.CAN_WAIT
            reason = "Completed ${kmSince} km ago. Still has approximately ${(suggestedKm - kmSince).coerceAtLeast(0)} km of expected life remaining."
        } else if (kmSince >= suggestedKm) {
            status = QuoteVerdictStatus.LEGITIMATE_DUE
            reason = "Overdue/Due: ${kmSince} km since last service (recommended every ${suggestedKm} km)."
        } else {
            status = QuoteVerdictStatus.VERIFY_FIRST
            reason = "Nearing interval (${kmSince} / ${suggestedKm} km). Inspect visual condition before authorizing."
        }

        val question = getQuestionFor(item.serviceType, status, kmSince)

        return QuoteLineVerdict(
            item = item,
            status = status,
            reason = reason,
            questionToAsk = question,
            fairPriceRangeCents = fairRange,
            lastDoneOdometer = lastService.odometerAtService,
            lastDoneDate = lastService.serviceDate,
            kmSinceLastDone = kmSince
        )
    }

    private fun getFairPriceRange(serviceType: ServiceType): IntRange {
        return when (serviceType) {
            ServiceType.OIL_CHANGE -> 4500..8500        // $45 - $85
            ServiceType.TIRE_ROTATION -> 2500..5000     // $25 - $50
            ServiceType.BRAKE_SERVICE -> 14000..32000   // $140 - $320
            ServiceType.BATTERY -> 13000..23000         // $130 - $230
            ServiceType.AIR_FILTER -> 2500..5500        // $25 - $55
            ServiceType.CABIN_FILTER -> 3000..6000      // $30 - $60
            ServiceType.TRANSMISSION -> 16000..30000    // $160 - $300
            ServiceType.COOLANT -> 10000..19000         // $100 - $190
            ServiceType.SPARK_PLUGS -> 12000..26000     // $120 - $260
            ServiceType.TIMING_BELT -> 50000..110000    // $500 - $1100
            ServiceType.WIPER_BLADES -> 2000..4500      // $20 - $45
            ServiceType.INSPECTION -> 4000..10000       // $40 - $100
            ServiceType.EMISSIONS_TEST -> 3000..7000    // $30 - $70
            else -> 3000..15000
        }
    }

    private fun getQuestionFor(
        serviceType: ServiceType,
        status: QuoteVerdictStatus,
        kmSince: Int?
    ): String {
        return when (status) {
            QuoteVerdictStatus.LIKELY_UPSELL -> {
                if (kmSince != null) {
                    "\"My log shows this was performed $kmSince km ago. Can you show me the physical defect or test measurement that requires early replacement?\""
                } else {
                    "\"What specific inspection reading or wear measurement triggered this recommendation today?\""
                }
            }
            QuoteVerdictStatus.VERIFY_FIRST -> {
                when (serviceType) {
                    ServiceType.BRAKE_SERVICE -> "\"What is the remaining pad thickness in millimeters, and are the rotors grooved or below spec?\""
                    ServiceType.AIR_FILTER, ServiceType.CABIN_FILTER -> "\"Can you pull the filter out so I can see the dust saturation level before approving?\""
                    ServiceType.BATTERY -> "\"What was the CCA (Cold Cranking Amps) test reading compared to the battery's rated spec?\""
                    ServiceType.COOLANT, ServiceType.TRANSMISSION -> "\"Is the fluid discolored or failed a strip test, or is this recommended purely on mileage?\""
                    else -> "\"Is this required for safety today, or can it wait until the next oil change interval?\""
                }
            }
            QuoteVerdictStatus.CAN_WAIT -> {
                "\"Can we defer this item until my next scheduled visit in a few months, or does it present an immediate safety concern?\""
            }
            QuoteVerdictStatus.LEGITIMATE_DUE -> {
                "\"Does this quote include OEM-spec parts, fluid disposal, and labor with warranty?\""
            }
        }
    }

    private fun buildTalkingPoints(verdicts: List<QuoteLineVerdict>): List<String> {
        val points = mutableListOf<String>()

        val upsells = verdicts.filter { it.status == QuoteVerdictStatus.LIKELY_UPSELL }
        if (upsells.isNotEmpty()) {
            val names = upsells.joinToString(", ") { it.item.serviceType.label }
            points.add("Decline or question premature replacements: $names (completed recently in your service history).")
        }

        val verifies = verdicts.filter { it.status == QuoteVerdictStatus.VERIFY_FIRST }
        if (verifies.isNotEmpty()) {
            points.add("Ask for physical evidence (e.g. pad mm thickness, fluid test strip) before authorizing ${verifies.first().item.serviceType.label}.")
        }

        val highQuotes = verdicts.filter { it.item.priceCents > it.fairPriceRangeCents.last }
        if (highQuotes.isNotEmpty()) {
            val names = highQuotes.joinToString(", ") { it.item.serviceType.label }
            points.add("Price check: Quoted price for $names is above regional benchmark. Inquire about labor rate breakdown.")
        }

        if (points.isEmpty()) {
            points.add("All quoted line items appear consistent with vehicle maintenance schedule.")
        }

        return points
    }
}
