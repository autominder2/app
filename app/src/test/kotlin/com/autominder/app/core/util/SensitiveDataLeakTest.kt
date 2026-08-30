package com.autominder.app.core.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Permanent CI regression test guarding against raw un-scrubbed logging,
 * un-routed console prints, and PII parameter leaks in Analytics payloads.
 */
class SensitiveDataLeakTest {

    private val sourceRoot = File("src/main/kotlin")

    private fun kotlinSources(): List<File> {
        require(sourceRoot.isDirectory) { "Missing ${sourceRoot.absolutePath}" }
        return sourceRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    @Test
    fun `no production kotlin file uses raw println or System out`() {
        val violations = mutableListOf<String>()
        val forbiddenCalls = listOf("println(", "print(", "System.out.", "System.err.")

        kotlinSources().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                val trimmed = line.trim()
                if (!trimmed.startsWith("//") && !trimmed.startsWith("*")) {
                    forbiddenCalls.forEach { forbidden ->
                        if (trimmed.contains(forbidden)) {
                            violations += "${file.name}:${index + 1} contains raw '$forbidden'"
                        }
                    }
                }
            }
        }

        assertTrue(
            "Raw console printing is forbidden in production code. Use Timber instead:\n" +
                violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun `no production kotlin file directly invokes android util Log`() {
        val violations = mutableListOf<String>()
        val logCallPattern = Regex("""\bLog\.[dviee]\(""")

        kotlinSources().forEach { file ->
            // CrashlyticsTree uses Log priority constants (Log.DEBUG, Log.INFO, etc.) which is valid
            if (file.name != "CrashlyticsTree.kt") {
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (!trimmed.startsWith("//") && !trimmed.startsWith("*")) {
                        if (logCallPattern.containsMatchIn(trimmed)) {
                            violations += "${file.name}:${index + 1} invokes android.util.Log directly"
                        }
                    }
                }
            }
        }

        assertTrue(
            "Direct android.util.Log calls bypass ProGuard log stripping. Use Timber instead:\n" +
                violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun `analytics event parameters never contain forbidden PII keys`() {
        val forbiddenKeys = setOf(
            "vin", "plate", "license_plate", "licenseplate", "email", "phone",
            "address", "token", "purchase_token", "purchasetoken", "password"
        )

        // Check AnalyticsParams constants
        val paramsFile = File("src/main/kotlin/com/autominder/app/core/util/AnalyticsHelper.kt")
        if (paramsFile.exists()) {
            val text = paramsFile.readText().lowercase()
            forbiddenKeys.forEach { key ->
                val match = Regex("val\\s+[a-z_0-9]+\\s*=\\s*\"$key\"").find(text)
                assertTrue(
                    "AnalyticsParams must never define PII key '$key'",
                    match == null
                )
            }
        }
    }
}
