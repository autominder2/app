package com.autominder.app.ui.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the one-way rule that makes distance display correct:
 *
 *   Every odometer value in the database is stored in km. Conversion happens
 *   only at the UI boundary, via [com.autominder.app.domain.util.DistanceUtil].
 *
 * The failure mode is silent and asymmetric. `DistanceFormat.grouped(x)` renders
 * whatever integer it is handed, and `DistanceUtil.unitLabel(unit)` renders
 * whatever unit the user picked. Hand it a raw km value and label it with the
 * user's unit and the app confidently prints a wrong number — never crashing,
 * never logging, and correct for the majority of users who are on km, so it
 * survives casual testing indefinitely.
 *
 * Two real instances were found on 2026-08-26, both by auditing the call sites
 * rather than by any test or crash:
 *
 *  - `VehicleListScreen` printed the next-service distance as
 *    `grouped(item.nextServiceRemainingKm)` with the user's unit label. That
 *    value is `nextDueOdometer - currentOdometer`, so it is km. On a miles
 *    device it read "in ~8,000 mi" when the truth was 8,000 km ≈ 4,971 mi — a
 *    61% overstatement, on the main Garage list. Line 307 of the same file
 *    converted correctly; line 334 did not.
 *
 *  - `shareServiceReceipt` hardcoded the literal "km" AND skipped the
 *    conversion, so a miles user saw "52,270 mi" on screen and shared
 *    "84,120 km" for the same record. Worse than the first, because that text
 *    leaves the device — to a buyer, or a mechanic.
 *
 * The convention this test enforces is the one the codebase already follows:
 * a value handed to `DistanceFormat.grouped` is either converted inline, or
 * held in a local whose name says it is a display value. A name like `lastKm`
 * on an already-converted value is the trap that produced both bugs, so
 * *-Km names on the way into a formatter are treated as unconverted.
 */
class OdometerUnitConversionTest {

    private val sourceRoot = File("src/main/kotlin")

    /**
     * Call sites where the argument is legitimately already in display units for
     * a reason the naming convention cannot express. Each needs a stated reason;
     * an entry without one is not an exemption, it is an unreviewed bug.
     */
    private val justified = mapOf(
        "AddServiceScreen.kt" to
            "uiState.odometer is the text the user typed into the odometer field, " +
            "which is by definition already in their chosen unit.",
        "AddServiceViewModel.kt" to
            "nextOdo = typed odometer + typed interval; both operands come from " +
            "user input in display units, so the sum is too."
    )

    /** Extracts the argument text of a call, respecting nested parentheses. */
    private fun argumentOf(source: String, openParenIndex: Int): String {
        var depth = 0
        var i = openParenIndex
        while (i < source.length) {
            when (source[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return source.substring(openParenIndex + 1, i)
                }
            }
            i++
        }
        return source.substring(openParenIndex + 1)
    }

    @Test
    fun `every odometer formatted for display is converted out of stored km`() {
        require(sourceRoot.isDirectory) { "missing ${sourceRoot.absolutePath}" }

        val marker = "DistanceFormat.grouped("
        val violations = mutableListOf<String>()

        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val source = file.readText()
                var from = 0
                while (true) {
                    val hit = source.indexOf(marker, from)
                    if (hit < 0) break
                    from = hit + marker.length

                    // A mention inside a comment documents the rule, it does not apply it.
                    val lineStart = source.lastIndexOf('\n', hit).let { if (it < 0) 0 else it + 1 }
                    val linePrefix = source.substring(lineStart, hit).trimStart()
                    if (linePrefix.startsWith("//") || linePrefix.startsWith("*")) continue

                    val arg = argumentOf(source, hit + marker.length - 1)
                    val converted = arg.contains("kmToDisplay")
                    val namedAsDisplay = arg.contains("display", ignoreCase = true)
                    val namedAsStoredKm = Regex("""\bKm\b|Km\)|RemainingKm|OdometerKm""")
                        .containsMatchIn(arg)

                    if (converted || (namedAsDisplay && !namedAsStoredKm)) continue
                    if (justified.containsKey(file.name)) continue

                    val line = source.substring(0, hit).count { it == '\n' } + 1
                    violations += "${file.name}:$line — grouped(${arg.trim().take(70)})"
                }
            }

        assertTrue(
            buildString {
                append("These call sites format a distance without converting it out of ")
                append("stored km. If the value is labelled with DistanceUtil.unitLabel(), ")
                append("every user on miles is shown a number ~61% too large:\n\n")
                violations.forEach { append("  - ").append(it).append('\n') }
                append("\nFix by wrapping the argument in ")
                append("DistanceUtil.kmToDisplay(value, distanceUnit), or — if the value ")
                append("genuinely is already in display units — name the local *Display ")
                append("and add the file to `justified` with the reason.")
            },
            violations.isEmpty()
        )
    }

    @Test
    fun `the shared service receipt does not hardcode a distance unit`() {
        val screen = File(
            "src/main/kotlin/com/autominder/app/ui/screens/service/ServiceDetailScreen.kt"
        )
        require(screen.exists()) { "ServiceDetailScreen.kt not found" }

        val activeCode = screen.readLines()
            .filterNot { val t = it.trimStart(); t.startsWith("//") || t.startsWith("*") }
            .joinToString("\n")

        assertTrue(
            "shareServiceReceipt must take the distance unit as a parameter. The " +
                "receipt is built from the same record the screen displays and is " +
                "sent to third parties, so it cannot assume km.",
            Regex("""fun shareServiceReceipt\([^)]*distanceUnit: String""", RegexOption.DOT_MATCHES_ALL)
                .containsMatchIn(activeCode)
        )

        assertTrue(
            "ServiceDetailScreen must not assign a hardcoded distance unit literal " +
                "(found `val distanceUnit = \"km\"`). Read LocalDistanceUnit instead.",
            !Regex("""val\s+distanceUnit\s*=\s*"(km|mi)"""").containsMatchIn(activeCode)
        )
    }
}
