package com.autominder.app.core.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Stops analytics events from being silently discarded by Firebase.
 *
 * Found on a device running the release build, 2026-08-26:
 *
 *   E FA: Name is reserved. Type, name: event, session_start
 *   E FA: Invalid public event name. Event will not be logged (FE): session_start
 *
 * `AppLifecycleObserver` called `logEvent("session_start")` on every foreground.
 * `session_start` is reserved by Firebase, so every one was rejected. Nothing in
 * the app noticed: the call returned normally, the SDK dropped the event, and the
 * dashboard just had no sessions. A metric you believe you are collecting and are
 * not is worse than no metric, because you make decisions on it.
 *
 * This is a JVM test that reads the sources rather than exercising the SDK,
 * because the failure is a *name*. Firebase validates names at runtime inside a
 * library we do not control, so nothing at compile time or in a mocked test
 * would catch a newly-added reserved name.
 */
class AnalyticsEventNameTest {

    /**
     * Firebase's reserved event names, plus the prefixes it forbids.
     * Sourced from Firebase Analytics' documented reserved list.
     */
    private val reservedNames = setOf(
        "ad_activeview", "ad_click", "ad_exposure", "ad_impression", "ad_query",
        "ad_reward", "adunit_exposure", "app_background", "app_clear_data",
        "app_exception", "app_remove", "app_store_refund", "app_store_subscription_cancel",
        "app_store_subscription_convert", "app_store_subscription_renew",
        "app_update", "app_upgrade", "dynamic_link_app_open",
        "dynamic_link_app_update", "dynamic_link_first_open", "error",
        "first_open", "first_visit", "in_app_purchase", "notification_dismiss",
        "notification_foreground", "notification_open", "notification_receive",
        "os_update", "session_start", "session_start_with_rollout",
        "user_engagement"
    )

    /** Prefixes Firebase reserves for its own use. */
    private val reservedPrefixes = listOf("firebase_", "google_", "ga_")

    private val sourceRoot = File("src/main/kotlin")

    @Test
    fun `no analytics event uses a name Firebase will reject`() {
        require(sourceRoot.isDirectory) { "missing ${sourceRoot.absolutePath}" }

        // Matches logEvent("literal_name" ...). Only literals are checkable, and
        // only literals are used in this codebase.
        val call = Regex("""logEvent\(\s*"([A-Za-z0-9_]+)"""")
        val offenders = mutableListOf<String>()

        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    // Skip comment lines: the fix documents the bad name on purpose.
                    val code = line.trimStart()
                    if (code.startsWith("//") || code.startsWith("*")) return@forEachIndexed

                    call.findAll(line).forEach { m ->
                        val name = m.groupValues[1]
                        val why = when {
                            name in reservedNames ->
                                "\"$name\" is a reserved Firebase event name"
                            reservedPrefixes.any { name.startsWith(it) } ->
                                "\"$name\" uses a reserved Firebase prefix"
                            name.length > 40 ->
                                "\"$name\" exceeds Firebase's 40-character event-name limit"
                            !name.first().isLetter() ->
                                "\"$name\" must start with a letter"
                            else -> null
                        }
                        if (why != null) offenders += "${file.path}:${index + 1} — $why"
                    }
                }
            }

        assertTrue(
            buildString {
                append("These analytics events would be DISCARDED by Firebase at runtime, ")
                append("with no error visible inside the app:\n\n")
                offenders.forEach { append("  - ").append(it).append('\n') }
                append("\nRename with a first-party prefix (the codebase uses `am_`). ")
                append("Firebase already auto-collects session_start, first_open and ")
                append("user_engagement — do not re-emit them by hand.")
            },
            offenders.isEmpty()
        )
    }

    @Test
    fun `the reserved session_start call has not come back`() {
        val observer = File(
            "src/main/kotlin/com/autominder/app/core/util/AppLifecycleObserver.kt"
        )
        require(observer.exists()) { "AppLifecycleObserver.kt not found" }

        val activeCode = observer.readLines()
            .filterNot { val t = it.trimStart(); t.startsWith("//") || t.startsWith("*") }
            .joinToString("\n")

        assertTrue(
            "AppLifecycleObserver must not log \"session_start\" — Firebase reserves " +
                "the name and collects it automatically.",
            !activeCode.contains("\"session_start\"")
        )
    }
}
