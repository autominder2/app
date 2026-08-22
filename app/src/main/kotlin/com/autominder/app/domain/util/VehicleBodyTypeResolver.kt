package com.autominder.app.domain.util

import com.autominder.app.domain.model.VehicleBodyType

/**
 * Pure-Kotlin resolver: infers [VehicleBodyType] from vehicle Make + Model
 * strings using curated keyword sets.
 *
 * 2026 best practice: body-type intelligence lives in the domain layer, not
 * the UI layer. Zero Android dependencies — fully unit-testable.
 *
 * Keyword matching is case-insensitive and operates on padded text so that
 * short word tokens (e.g. "z4", "m4", "fit", "500", "86") match whole words
 * accurately without matching substrings (like "profit" or "benefit").
 */
object VehicleBodyTypeResolver {

    /**
     * Resolves the [VehicleBodyType] for a given make and model.
     * Returns [VehicleBodyType.DEFAULT] (Sedan) when inputs are blank or unknown.
     */
    fun resolve(make: String?, model: String?): VehicleBodyType {
        val raw = "${make.orEmpty()} ${model.orEmpty()}".lowercase().trim()
        if (raw.isBlank()) return VehicleBodyType.DEFAULT

        val padded = " $raw "

        return when {
            matchesAny(padded, raw, MOTORCYCLE_KEYWORDS) -> VehicleBodyType.MOTORCYCLE
            matchesAny(padded, raw, TRUCK_KEYWORDS)      -> VehicleBodyType.TRUCK
            matchesAny(padded, raw, MINIVAN_KEYWORDS)    -> VehicleBodyType.MINIVAN
            matchesAny(padded, raw, CONVERTIBLE_KEYWORDS)-> VehicleBodyType.CONVERTIBLE
            matchesAny(padded, raw, COUPE_KEYWORDS)      -> VehicleBodyType.COUPE
            matchesAny(padded, raw, HATCHBACK_KEYWORDS)  -> VehicleBodyType.HATCHBACK
            matchesAny(padded, raw, SUV_KEYWORDS)        -> VehicleBodyType.SUV
            else                                         -> VehicleBodyType.DEFAULT
        }
    }

    private fun matchesAny(padded: String, raw: String, keywords: Set<String>): Boolean {
        return keywords.any { kw ->
            if (kw.startsWith(" ") || kw.endsWith(" ")) {
                padded.contains(kw)
            } else {
                raw.contains(kw)
            }
        }
    }

    // ─── Motorcycle / Two-wheel ───────────────────────────────────────────────
    private val MOTORCYCLE_KEYWORDS = setOf(
        "ninja", "cbr", "cbf", "cb500", "cb650", "cb1000",
        "hayabusa", "gsxr", "gsx-r", "gsx-s",
        " r1 ", " r6 ", " r3 ", " r7 ", "yzf-r",
        "mt-07", "mt-09", "mt-10", "mt07", "mt09",
        "ducati", "panigale", "monster", "multistrada", "hypermotard", "scrambler",
        "harley", "sportster", "street glide", "fat boy", "fat bob", "road king",
        "indian scout", "indian chief",
        "triumph", "bonneville", "thruxton", "street triple", "speed triple",
        "r1250", "r1200", "r1300", "s1000rr", "f750gs", "f850gs",
        "ktm duke", "ktm exc", "ktm adventure",
        "kawasaki vulcan", "kawasaki versys",
        "suzuki sv", "boulevard", "v-strom",
        "africa twin", "nc750", "pcx",
        "vespa", "piaggio", "aprilia",
        "royal enfield", "meteor", "himalayan",
        "bmw gs", "bmw s1000"
    )

    // ─── Pickup Trucks ────────────────────────────────────────────────────────
    private val TRUCK_KEYWORDS = setOf(
        "f-150", "f150", "f-250", "f250", "f-350", "f350", "f-450", "f450",
        "f-series", "fseries", " ranger ", "maverick",
        "silverado", " colorado ", "avalanche",
        " sierra ", " canyon ",
        "tacoma", "tundra", "hilux", "land cruiser pickup",
        "ram 1500", "ram 2500", "ram 3500", "ram pickup", "dodge ram",
        "frontier", " titan ",
        "ridgeline",
        "cybertruck",
        "l200", "triton", "d-max", "isuzu d-max",
        "amarok", "ranger raptor",
        "navara", "np300",
        "bt-50",
        "pickup"
    )

    // ─── Minivans ─────────────────────────────────────────────────────────────
    private val MINIVAN_KEYWORDS = setOf(
        "sienna", "odyssey", "pacifica", "grand caravan",
        "carnival", "sedona",
        "metris", "vito", "viano",
        "town & country", "town and country",
        "quest", "previa", "estima",
        "routan", "voyager",
        "minivan", "people mover"
    )

    // ─── Convertibles / Roadsters ─────────────────────────────────────────────
    private val CONVERTIBLE_KEYWORDS = setOf(
        "miata", "mx-5", "mx5",
        "boxster", "cabriolet", "cabrio", "convertible", "roadster", "spyder", "spider",
        "solstice", " pontiac sky ", " saturn sky ",
        " z4 ", " slk ", " slc ",
        "718 spyder", "huracan spyder", "aventador roadster",
        "crossfire conv"
    )

    // ─── Coupes (2-door, sporty) ──────────────────────────────────────────────
    private val COUPE_KEYWORDS = setOf(
        "mustang", "challenger", "camaro",
        "brz", "gr86", " 86 ", "ft86", "frs",
        "supra", "celica", "mr2",
        "integra type r", "nsx",
        "veloster",
        " m2 ", " m3 coupe", " m4 ", " m6 ",
        "4 series", "2 series coupe",
        " 911 ", " 718 ", "panamera",
        "lfa", "corvette", "viper", "ford gt",
        " tt ", " r8 "
    )

    // ─── Hatchbacks ───────────────────────────────────────────────────────────
    private val HATCHBACK_KEYWORDS = setOf(
        "golf", "gti", "polo", " up ", "lupo",
        " fit ", "jazz", "yaris", "vitz",
        "fiesta", "focus hatch",
        "punto", " 500 ", "fiat 500",
        " i3 ", "leaf", "bolt ev", "e-golf", "id.3",
        "ioniq 5", "ioniq 6",
        "e-208", "clio", "megane",
        "swift", "baleno",
        "micra", "picanto", "aygo", "zoe"
    )

    // ─── SUVs / Crossovers ────────────────────────────────────────────────────
    private val SUV_KEYWORDS = setOf(
        "rav4", "cr-v", "crv", "hrv", "hr-v", "pilot", "passport",
        "explorer", "escape", "edge", "bronco", "expedition",
        "highlander", "4runner", "land cruiser", "fortuner",
        "model y", "model x",
        " x5 ", " x3 ", " x1 ", " x6 ", " x7 ",
        "glc", "gle", "gls", "gla", "glb",
        " q5 ", " q7 ", " q3 ", " q8 ",
        "tiguan", "touareg", "t-roc", "t-cross",
        "outlander", "eclipse cross", "asx",
        "palisade", "tucson", "santa fe", "kona",
        "sorento", "telluride", "sportage",
        "cx-5", "cx5", "cx-9", "cx9", "cx-30", "cx-50",
        "forester", "outback", "ascent", "crosstrek",
        "rogue", "murano", "pathfinder", "armada", "patrol",
        "defender", "discovery", "range rover", "evoque", "velar",
        "cherokee", "wrangler", "wagoneer", "compass",
        "escalade", "tahoe", "suburban", "yukon", "sequoia",
        "cayenne", "macan", "stelvio", "tonale", "durango"
    )
}
