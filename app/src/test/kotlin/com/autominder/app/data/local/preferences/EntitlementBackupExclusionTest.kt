package com.autominder.app.data.local.preferences

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the entitlement cache against being restored from a backup.
 *
 * The defect this pins, found 2026-08-26:
 *
 * `is_pro_cached` was a key in the `user_preferences` DataStore. Both backup
 * configs include `datastore/` wholesale, and both carried a comment asserting
 * that nothing stored locally needed excluding — true when written, false once
 * a cached Pro flag was added.
 *
 * That made a complete entitlement bypass available with no tools whatsoever:
 *
 *   1. Subscribe. `setProCached(true)` writes the flag.
 *   2. Let Android back the app up, then cancel or refund.
 *   3. Restore the backup — on this device or, via device-transfer, a second one.
 *   4. `initialize()` reads the cache on cold start and grants Pro immediately.
 *   5. Stay offline. `queryExistingPurchases` downgrades only on *complete*
 *      evidence (both the SUBS and INAPP queries returning OK) — a deliberate
 *      and correct choice, so a paying user on a flaky connection is never
 *      locked out, but it also means nothing ever contradicts the restored
 *      flag. Airplane mode makes the grant permanent.
 *
 * The fix is a separate DataStore file, excluded by path, so the rest of
 * `user_preferences` (theme, units, onboarding) still restores — losing that is
 * the churn the backup rules exist to prevent.
 *
 * These assertions read the source and resource files directly rather than
 * exercising DataStore. That is deliberate: the regression being guarded is a
 * *configuration* change — re-adding the include, renaming the store, or moving
 * the key back into `user_preferences`. None of those would fail a runtime test
 * that only checks the flag round-trips, and all of them reopen the bypass.
 */
class EntitlementBackupExclusionTest {

    /** Must match the `preferencesDataStore(name = …)` for the entitlement store. */
    private val storeName = "entitlement_cache"
    private val excludedPath = "datastore/$storeName.preferences_pb"

    private fun read(path: String): String {
        val f = File(path)
        require(f.exists()) { "$path not found at ${f.absolutePath}" }
        return f.readText()
    }

    private val backupRules by lazy { read("src/main/res/xml/backup_rules.xml") }
    private val extractionRules by lazy { read("src/main/res/xml/data_extraction_rules.xml") }
    private val userPreferences by lazy {
        read("src/main/kotlin/com/autominder/app/data/local/preferences/UserPreferences.kt")
    }

    private fun excludesEntitlement(xml: String) =
        Regex("""<exclude\s+domain="file"\s+path="$excludedPath"\s*/>""").containsMatchIn(xml)

    @Test
    fun `android 11 auto backup excludes the entitlement cache`() {
        assertTrue(
            "backup_rules.xml must exclude $excludedPath. Without it, restoring a " +
                "backup grants Pro and no offline reconcile will ever take it back.",
            excludesEntitlement(backupRules)
        )
    }

    @Test
    fun `android 12 cloud backup excludes the entitlement cache`() {
        val cloud = section(extractionRules, "cloud-backup")
        assertTrue(
            "The <cloud-backup> block must exclude $excludedPath",
            excludesEntitlement(cloud)
        )
    }

    @Test
    fun `device transfer excludes the entitlement cache`() {
        // Easy to forget, and the worse of the two: device-transfer copies the
        // file to a second physical device, turning one purchase into two
        // simultaneous entitlements rather than one revived expired entitlement.
        val transfer = section(extractionRules, "device-transfer")
        assertTrue(
            "The <device-transfer> block must exclude $excludedPath, or one " +
                "purchase yields Pro on two devices at once.",
            excludesEntitlement(transfer)
        )
    }

    @Test
    fun `the entitlement store is a separate file from user preferences`() {
        assertTrue(
            "UserPreferences must declare a DataStore named \"$storeName\". The " +
                "excluded backup path is derived from this name, so renaming the " +
                "store without updating both XML files silently reopens the bypass.",
            userPreferences.contains("name = \"$storeName\"")
        )
        assertTrue(
            "user_preferences must remain a distinct store so theme/units/onboarding " +
                "still restore on a device upgrade",
            userPreferences.contains("name = \"user_preferences\"")
        )
    }

    @Test
    fun `the cached pro flag is never read or written through the backed-up store`() {
        // Both accessors must go through entitlementStore. A single
        // `context.dataStore` here would put the flag back in the backed-up
        // file while every XML exclusion above still looked correct.
        val accessors = Regex(
            """(?:val isProCached[^\n]*|suspend fun setProCached[^{]*\{)[\s\S]{0,220}?IS_PRO_CACHED"""
        ).findAll(userPreferences).map { it.value }.toList()

        assertTrue(
            "Expected both isProCached and setProCached to reference IS_PRO_CACHED; " +
                "found ${accessors.size}. Update this test if the accessors moved.",
            accessors.size >= 2
        )

        accessors.forEach { block ->
            assertTrue(
                "An IS_PRO_CACHED accessor does not use entitlementStore:\n$block",
                block.contains("entitlementStore")
            )
            assertFalse(
                "An IS_PRO_CACHED accessor still uses the backed-up dataStore:\n$block",
                Regex("""context\.dataStore""").containsMatchIn(block)
            )
        }
    }

    private fun section(xml: String, tag: String): String {
        val match = Regex("<$tag>([\\s\\S]*?)</$tag>").find(xml)
        requireNotNull(match) { "<$tag> block missing from data_extraction_rules.xml" }
        return match.groupValues[1]
    }
}
