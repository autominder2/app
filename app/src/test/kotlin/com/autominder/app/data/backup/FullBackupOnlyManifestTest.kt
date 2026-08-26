package com.autominder.app.data.backup

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Pins the manifest attribute without which nothing is ever backed up.
 *
 * Found on a device, 2026-08-26. Every piece of static evidence said backup was
 * healthy: `allowBackup="true"`, a `fullBackupContent` rules file, a
 * `dataExtractionRules` file, a custom agent that checkpoints the WAL at exactly
 * the right moment, and unit tests asserting the include/exclude paths. The
 * store listing and the privacy policy both promise records survive a phone
 * upgrade. It did not work:
 *
 *     $ adb shell bmgr backupnow com.autominder.app
 *     Running incremental backup for 1 requested packages.
 *     Package com.autominder.app with result: Success
 *     Backup finished with result: Success
 *     $ adb shell pm clear com.autominder.app
 *     $ adb shell bmgr restore 1 com.autominder.app
 *     restoreStarting: 1 packages
 *     restoreFinished: 0
 *     $ adb shell run-as com.autominder.app ls files/datastore/
 *     ls: files/datastore/: No such file or directory
 *
 * Cause: declaring `android:backupAgent` opts an app into the legacy key/value
 * backup API. [AutoMinderBackupAgent] implements only `onFullBackup`; its
 * `onBackup` is deliberately `Unit`, because the app is file-based. So the
 * system ran the key/value path, the agent wrote nothing, and the backup
 * *succeeded* with an empty payload. `android:fullBackupOnly="true"` is the
 * documented way to tell the platform an app with a custom agent wants Auto
 * Backup, and it is the only thing standing between these rules files and a
 * silent no-op.
 *
 * This failure mode is invisible from inside the app: no exception, no log, and
 * a success result from the backup manager. It surfaces only when a user
 * migrates to a new phone and finds their service history gone -- which is the
 * single worst moment to discover it, and the top churn driver in this category.
 * Hence a test on the attribute itself.
 */
class FullBackupOnlyManifestTest {

    private val manifest: String by lazy {
        val f = File("src/main/AndroidManifest.xml")
        require(f.exists()) { "AndroidManifest.xml not found at ${f.absolutePath}" }
        // Strip comments: this defect is *documented* in a comment right above
        // the attribute, so a naive contains() check would pass on the prose
        // alone even if the attribute were deleted.
        f.readText().replace(Regex("""<!--.*?-->""", RegexOption.DOT_MATCHES_ALL), "")
    }

    @Test
    fun `a custom backup agent must be paired with fullBackupOnly`() {
        val declaresAgent = Regex("""android:backupAgent\s*=""").containsMatchIn(manifest)
        if (!declaresAgent) return // No agent, no ambiguity to resolve.

        assertTrue(
            "AndroidManifest declares android:backupAgent but not " +
                "android:fullBackupOnly=\"true\". That opts the app into legacy " +
                "key/value backup, where AutoMinderBackupAgent.onBackup writes " +
                "nothing -- so backup reports success and restores an empty " +
                "payload, and every user record is silently lost on a device " +
                "upgrade. Either set fullBackupOnly=\"true\" or implement " +
                "onBackup/onRestore for real.",
            Regex("""android:fullBackupOnly\s*=\s*"true"""").containsMatchIn(manifest)
        )
    }

    @Test
    fun `backup stays enabled and both rules files stay wired up`() {
        assertTrue(
            "android:allowBackup must remain true -- records surviving a phone " +
                "upgrade is a promise made in the store listing and the privacy policy.",
            Regex("""android:allowBackup\s*=\s*"true"""").containsMatchIn(manifest)
        )
        assertTrue(
            "android:fullBackupContent must point at @xml/backup_rules (Android 11 and below).",
            Regex("""android:fullBackupContent\s*=\s*"@xml/backup_rules"""").containsMatchIn(manifest)
        )
        assertTrue(
            "android:dataExtractionRules must point at @xml/data_extraction_rules " +
                "(Android 12+ cloud backup and device transfer).",
            Regex("""android:dataExtractionRules\s*=\s*"@xml/data_extraction_rules"""")
                .containsMatchIn(manifest)
        )
    }

    @Test
    fun `the agent still implements the full-backup callback it claims`() {
        val agent = File(
            "src/main/kotlin/com/autominder/app/data/backup/AutoMinderBackupAgent.kt"
        )
        require(agent.exists()) { "AutoMinderBackupAgent.kt not found" }
        val source = agent.readText()

        assertTrue(
            "AutoMinderBackupAgent must override onFullBackup. With " +
                "fullBackupOnly=\"true\" this is the only backup callback the " +
                "platform invokes, so losing it means the WAL is never " +
                "checkpointed and the newest records are missing from the backup.",
            source.contains("override fun onFullBackup")
        )
        assertTrue(
            "onFullBackup must call super.onFullBackup(data) -- the platform " +
                "performs the actual file copy that honours backup_rules.xml. " +
                "Checkpointing without delegating backs up nothing.",
            source.contains("super.onFullBackup(data)")
        )
    }
}
