package com.autominder.app.domain.util

/**
 * Pure, static domain catalog of vehicle makes and models.
 * Replaces hardcoded ad-hoc UI lists with a curated, deterministic dataset.
 *
 * Stateless and thread-safe.
 */
object VehicleCatalog {

    /**
     * Curated head of popular makes shown as quick-pick chips and default sheet head.
     * Preserves the 14 legacy brands as a baseline.
     */
    val popularMakes: List<String> = listOf(
        "Toyota",
        "Honda",
        "Ford",
        "Chevrolet",
        "Nissan",
        "Hyundai",
        "Kia",
        "Volkswagen",
        "BMW",
        "Mercedes-Benz",
        "Tesla",
        "Mazda",
        "Subaru",
        "Jeep"
    )

    /**
     * Comprehensive alphabetical list of automotive makes (~50 makes).
     * Guaranteed to be a superset of popularMakes and all legacy lists.
     */
    val allMakes: List<String> = listOf(
        "Acura",
        "Alfa Romeo",
        "Aston Martin",
        "Audi",
        "Bentley",
        "BMW",
        "Buick",
        "Cadillac",
        "Chevrolet",
        "Chrysler",
        "Dodge",
        "Ferrari",
        "Fiat",
        "Ford",
        "Genesis",
        "GMC",
        "Honda",
        "Hyundai",
        "Infiniti",
        "Jaguar",
        "Jeep",
        "Kia",
        "Lamborghini",
        "Land Rover",
        "Lexus",
        "Lincoln",
        "Lucid",
        "Maserati",
        "Mazda",
        "McLaren",
        "Mercedes-Benz",
        "MINI",
        "Mitsubishi",
        "Nissan",
        "Polestar",
        "Pontiac",
        "Porsche",
        "RAM",
        "Rivian",
        "Rolls-Royce",
        "Saab",
        "Saturn",
        "Scion",
        "Subaru",
        "Suzuki",
        "Tesla",
        "Toyota",
        "Volkswagen",
        "Volvo"
    )

    private val makeToModelsMap: Map<String, List<String>> = mapOf(
        "Toyota" to listOf("RAV4", "Camry", "Corolla", "Highlander", "Tacoma", "Prius", "4Runner", "Tundra", "Sienna", "Grand Highlander"),
        "Honda" to listOf("CR-V", "Civic", "Accord", "Pilot", "HR-V", "Odyssey", "Passport", "Ridgeline"),
        "Ford" to listOf("F-150", "Explorer", "Escape", "Mustang", "Bronco", "Edge", "Ranger", "Maverick", "Expedition", "F-150 Lightning"),
        "Chevrolet" to listOf("Silverado", "Equinox", "Malibu", "Tahoe", "Traverse", "Colorado", "Suburban", "Blazer", "Corvette", "Trailblazer"),
        "Nissan" to listOf("Rogue", "Altima", "Sentra", "Pathfinder", "Frontier", "Kicks", "Murano", "Armada"),
        "Hyundai" to listOf("Tucson", "Elantra", "Santa Fe", "Sonata", "Kona", "Palisade", "Ioniq 5", "Venue", "Santa Cruz"),
        "Kia" to listOf("Sportage", "Forte", "Telluride", "Sorento", "Soul", "K5", "Carnival", "EV6", "Seltos"),
        "BMW" to listOf("3 Series", "X3", "5 Series", "X5", "X1", "4 Series", "X7", "M3", "M5", "i4", "iX"),
        "Mercedes-Benz" to listOf("C-Class", "E-Class", "GLC", "GLE", "A-Class", "S-Class", "GLA", "GLB", "GLS", "CLA", "EQE", "EQS"),
        "Mercedes" to listOf("C-Class", "E-Class", "GLC", "GLE", "A-Class", "S-Class", "GLA", "GLB", "GLS", "CLA"),
        "Volkswagen" to listOf("Golf", "Jetta", "Tiguan", "Passat", "Atlas", "Taos", "ID.4", "GTI", "Atlas Cross Sport"),
        "Tesla" to listOf("Model 3", "Model Y", "Model S", "Model X", "Cybertruck"),
        "Mazda" to listOf("CX-5", "Mazda3", "CX-30", "CX-50", "CX-90", "Miata MX-5", "Mazda6"),
        "Subaru" to listOf("Outback", "Forester", "Crosstrek", "Impreza", "Ascent", "WRX", "Legacy", "BRZ"),
        "Jeep" to listOf("Grand Cherokee", "Wrangler", "Cherokee", "Compass", "Gladiator", "Renegade", "Grand Wagoneer"),
        "Audi" to listOf("A4", "Q5", "A6", "Q7", "A3", "Q3", "e-tron", "Q8", "A5", "S4", "S5"),
        "Lexus" to listOf("RX", "NX", "ES", "GX", "IS", "UX", "TX", "LX", "RC", "LC"),
        "GMC" to listOf("Sierra 1500", "Terrain", "Acadia", "Yukon", "Canyon", "Yukon XL", "Hummer EV"),
        "Dodge" to listOf("Charger", "Challenger", "Durango", "Hornet", "Grand Caravan", "Journey"),
        "RAM" to listOf("1500", "2500", "3500", "ProMaster"),
        "Acura" to listOf("MDX", "RDX", "Integra", "TLX", "ILX"),
        "Cadillac" to listOf("Escalade", "XT5", "XT4", "CT5", "XT6", "CT4", "Lyriq"),
        "Volvo" to listOf("XC90", "XC60", "XC40", "S60", "V60", "S90", "C40 Recharge", "EX30"),
        "Porsche" to listOf("911", "Cayenne", "Macan", "Panamera", "Taycan", "718 Boxster", "718 Cayman"),
        "Lincoln" to listOf("Navigator", "Aviator", "Corsair", "Nautilus"),
        "Buick" to listOf("Enclave", "Encore GX", "Envision", "Envista"),
        "Genesis" to listOf("GV70", "GV80", "G70", "G80", "G90", "GV60"),
        "Infiniti" to listOf("QX60", "QX50", "Q50", "QX80", "QX55"),
        "Land Rover" to listOf("Range Rover", "Defender", "Range Rover Sport", "Range Rover Evoque", "Discovery", "Discovery Sport"),
        "MINI" to listOf("Cooper", "Countryman", "Clubman", "Cooper S"),
        "Mitsubishi" to listOf("Outlander", "Eclipse Cross", "Outlander Sport", "Mirage"),
        "Rivian" to listOf("R1T", "R1S"),
        "Lucid" to listOf("Air", "Gravity"),
        "Polestar" to listOf("Polestar 2", "Polestar 3", "Polestar 4"),
        "Chrysler" to listOf("Pacifica", "300", "Voyager", "Town & Country")
    )

    /**
     * Search makes matching the given [query].
     * Case-insensitive. Ranks prefix matches before contains matches.
     * Blank query returns [allMakes].
     */
    fun searchMakes(query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return allMakes

        val prefixMatches = mutableListOf<String>()
        val containsMatches = mutableListOf<String>()

        for (make in allMakes) {
            if (make.startsWith(trimmed, ignoreCase = true)) {
                prefixMatches.add(make)
            } else if (make.contains(trimmed, ignoreCase = true)) {
                containsMatches.add(make)
            }
        }
        return prefixMatches + containsMatches
    }

    /**
     * Look up suggested models for a given [make].
     * Case-insensitive. Returns an empty list for unknown makes.
     */
    fun modelsForMake(make: String): List<String> {
        val trimmed = make.trim()
        if (trimmed.isEmpty()) return emptyList()

        val matchingKey = makeToModelsMap.keys.firstOrNull { it.equals(trimmed, ignoreCase = true) }
        return if (matchingKey != null) makeToModelsMap[matchingKey].orEmpty() else emptyList()
    }

    /**
     * Search models for a given [make] matching [query].
     * If [query] is blank, returns all known models for [make].
     */
    fun searchModels(make: String, query: String): List<String> {
        val models = modelsForMake(make)
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return models

        val prefixMatches = mutableListOf<String>()
        val containsMatches = mutableListOf<String>()

        for (model in models) {
            if (model.startsWith(trimmedQuery, ignoreCase = true)) {
                prefixMatches.add(model)
            } else if (model.contains(trimmedQuery, ignoreCase = true)) {
                containsMatches.add(model)
            }
        }
        return prefixMatches + containsMatches
    }
}
