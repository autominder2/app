package com.autominder.app.core.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Stops personal data from reaching Firebase through the release log tree.
 *
 * `CrashlyticsTree` drops VERBOSE and DEBUG, then forwards every remaining
 * message to `crashlytics.log(...)` as a breadcrumb and, at ERROR and above,
 * wraps it in a synthetic exception recorded as an Issue. So anything logged at
 * INFO or higher in a release build leaves the device.
 *
 * At the time of writing no call site leaks anything — the billing logs carry
 * response codes only. This test exists so that stays true. A single future
 * `Timber.i("restoring $vin")` would ship a VIN to a third-party service, and
 * nothing in the compiler, lint, or the Crashlytics SDK would object.
 *
 * `CLAUDE.md`: "Never log VINs, plates, notes, locations, tokens, personal
 * data." This is that rule, enforced.
 *
 * Scope note: DEBUG and VERBOSE are deliberately NOT checked. `CrashlyticsTree`
 * discards them before they leave the process, so `Timber.d` is a legitimate
 * place to print whatever helps during development.
 */
class NoPiiInReleaseLogsTest {

    /**
     * Identifier fragments that name personal data. Matched against the
     * segments of an interpolated expression, so `vehicleVin`, `owner_email`
     * and `purchaseToken` all trip; `debugMessage` and `responseCode` do not.
     */
    private val piiFragments = setOf(
        "vin", "plate", "licenseplate", "licence", "license",
        "note", "notes", "email", "phone",
        "token", "purchasetoken", "obfuscatedaccountid",
        "location", "latitude", "longitude", "lat", "lng", "address",
        "nickname", "ownername", "username", "password"
    )

    /** Only levels that survive into a release build. `d`/`v` are dropped. */
    private val leakingLevels = listOf("i", "w", "e", "wtf")

    private val sourceRoot = File("src/main/kotlin")

    private fun kotlinSources(): List<File> {
        require(sourceRoot.isDirectory) { "missing ${sourceRoot.absolutePath}" }
        return sourceRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    /** Splits `vehicle.ownerEmail` / `owner_email` into lowercase segments. */
    private fun segments(expression: String): List<String> =
        expression.split('.', '_', '(', ')', '[', ']', ' ', '?', '!', ',')
            .flatMap { part -> Regex("(?<=[a-z0-9])(?=[A-Z])").split(part) }
            .map { it.lowercase().trim() }
            .filter { it.isNotEmpty() }

    @Test
    fun `no release-level Timber call interpolates personal data`() {
        val levels = leakingLevels.joinToString("|")
        // Timber.i("…"), Timber.e(throwable, "…") — capture the argument list up
        // to the closing paren on the same logical line, which is how every
        // call in this codebase is written.
        val callPattern = Regex("""Timber\.($levels)\(([^\n]*)\)""")
        // Both `$identifier` and `${ expr }` forms.
        val interpolation = Regex("""\$\{([^}]*)}|\$([A-Za-z_][A-Za-z0-9_.]*)""")

        val offenders = mutableListOf<String>()

        kotlinSources().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                callPattern.findAll(line).forEach { call ->
                    val args = call.groupValues[2]
                    interpolation.findAll(args).forEach { interp ->
                        val expr = interp.groupValues[1].ifEmpty { interp.groupValues[2] }
                        val hit = segments(expr).firstOrNull { it in piiFragments }
                        if (hit != null) {
                            offenders += "${file.path}:${index + 1} logs \"$hit\" via " +
                                "`${expr.trim()}` at Timber.${call.groupValues[1]}()"
                        }
                    }
                }
            }
        }

        assertTrue(
            buildString {
                append("Personal data would be uploaded to Crashlytics as a breadcrumb ")
                append("(CrashlyticsTree forwards everything above DEBUG):\n\n")
                offenders.forEach { append("  - ").append(it).append('\n') }
                append("\nEither drop to Timber.d (discarded before leaving the device) ")
                append("or log a non-identifying substitute — a count, an id, a response ")
                append("code. If a fragment here is a false positive, narrow piiFragments ")
                append("rather than deleting the assertion.")
            },
            offenders.isEmpty()
        )
    }

    @Test
    fun `sentry never ships screen contents off the device`() {
        // Timber is not the only egress. Sentry can attach a screenshot and a
        // serialised view tree to an error report, both of which carry whatever
        // is on screen — and AutoMinder screens carry VINs, plates and notes.
        //
        // These two settings arrive as `true` in Sentry's own manifest template.
        // Someone disabled the screenshot with an explicit comment about VINs
        // and then left attach-view-hierarchy enabled two lines below it, so
        // this is a demonstrated failure mode, not a hypothetical one. The
        // published privacy policy now states that screen contents are not
        // transmitted, which makes both of these load-bearing.
        val manifest = File("src/main/AndroidManifest.xml")
        require(manifest.exists()) { "AndroidManifest.xml not found at ${manifest.absolutePath}" }
        val text = manifest.readText()

        listOf("io.sentry.attach-screenshot", "io.sentry.attach-view-hierarchy").forEach { key ->
            val value = Regex(
                """<meta-data\s+android:name="$key"\s+android:value="([^"]*)""""
            ).find(text)?.groupValues?.get(1)

            assertTrue(
                "$key must be declared and false. Screen contents must never reach a " +
                    "third-party service, and the privacy policy states they do not.",
                value == "false"
            )
        }
    }

    @Test
    fun `the release log tree still discards debug and verbose`() {
        // The test above only checks INFO and up. That is sound *because*
        // CrashlyticsTree filters below it — if that filter is ever removed,
        // every Timber.d in the app silently becomes an upload, and the
        // narrower scan above would no longer be protecting anything.
        val tree = File("src/main/kotlin/com/autominder/app/core/util/CrashlyticsTree.kt")
        require(tree.exists()) { "CrashlyticsTree.kt not found at ${tree.absolutePath}" }
        val body = tree.readText().replace(" ", "")

        assertTrue(
            "CrashlyticsTree must return early for priority <= Log.DEBUG. Without that " +
                "filter, every Timber.d call in the app becomes a Crashlytics upload and " +
                "`no release-level Timber call interpolates personal data` stops covering " +
                "the real surface.",
            body.contains("priority<=Log.DEBUG") && body.contains("return")
        )
    }
}
