package com.autominder.app.data.backup

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Checkpoints the write-ahead log immediately before Android copies our files.
 *
 * ## Why an agent, and not a "call this before the backup window"
 *
 * There is no backup window an app can schedule against. Auto Backup runs
 * opportunistically — device idle, charging, on an unmetered network, roughly
 * once a day — and the system never tells the app it is about to happen. Any
 * helper invoked from ordinary app code (on background, on a timer, from a
 * worker) is therefore only ever checkpointing at some *unrelated* moment, and
 * every write between that moment and the real backup is still stranded in the
 * WAL.
 *
 * [onFullBackup] is the only callback that runs *at* backup time, in our own
 * process, with the database available. That makes it the sole correct place
 * for the checkpoint.
 *
 * Device-to-device transfer uses the same callback, so a phone upgrade — the
 * scenario that actually loses people their service history — is covered by
 * the same code path.
 */
class AutoMinderBackupAgent : BackupAgent() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BackupAgentEntryPoint {
        fun backupCoordinator(): BackupCoordinator
    }

    override fun onFullBackup(data: FullBackupDataOutput?) {
        // Blocking is correct here: this is a binder callback on a background
        // thread, and the system expects onFullBackup to complete its work
        // before returning. Returning early would let the file copy start
        // against an un-flushed WAL, which is the bug this class exists to fix.
        runCatching {
            val coordinator = EntryPointAccessors
                .fromApplication(applicationContext, BackupAgentEntryPoint::class.java)
                .backupCoordinator()

            when (val result = runBlocking { coordinator.checkpoint() }) {
                is BackupCoordinator.Result.Success ->
                    Timber.d("Pre-backup checkpoint OK (%d frames)", result.framesCheckpointed)
                is BackupCoordinator.Result.Partial ->
                    Timber.i(
                        "Pre-backup checkpoint partial (%d/%d frames) — backup may omit the newest records",
                        result.framesCheckpointed, result.framesTotal
                    )
                is BackupCoordinator.Result.Failed ->
                    Timber.w(result.cause, "Pre-backup checkpoint failed — backing up database as-is")
            }
        }.onFailure { throwable ->
            // Never let our own preparation abort the user's backup. A stale
            // backup beats no backup.
            Timber.w(throwable, "Backup preparation failed; continuing with standard backup")
        }

        // Always hand off to the platform, which performs the actual file
        // backup honouring backup_rules.xml / data_extraction_rules.xml.
        super.onFullBackup(data)
    }

    // AutoMinder uses file-based (full) backup only — the legacy key/value API
    // is unused. These are abstract on BackupAgent, so they must exist.
    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) = Unit

    override fun onRestore(
        data: BackupDataInput?,
        appVersionCode: Int,
        newState: ParcelFileDescriptor?
    ) = Unit
}
