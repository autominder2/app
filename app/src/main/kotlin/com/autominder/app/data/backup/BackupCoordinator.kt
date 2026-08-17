package com.autominder.app.data.backup

import androidx.sqlite.db.SimpleSQLiteQuery
import com.autominder.app.core.di.IoDispatcher
import com.autominder.app.data.local.database.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Flushes the write-ahead log into the main database file.
 *
 * ## Why this exists
 *
 * The database runs in WAL mode (`DatabaseModule` sets
 * `JournalMode.WRITE_AHEAD_LOGGING`), so a committed write may live in
 * `autominder.db-wal` rather than `autominder.db`. Android's Auto Backup copies
 * files, not a transactionally consistent snapshot: it can capture the main
 * database and the WAL at different instants, or capture a `.db` whose recent
 * commits are still sitting in a WAL it did not copy.
 *
 * For an offline-first app that is the *entire* risk surface — Room holds the
 * only copy of the user's service history. A backup that silently drops the
 * last weeks of records is indistinguishable from a working backup until the
 * user restores onto a new phone and the records are gone.
 *
 * A `FULL` checkpoint blocks new writers, replays every committed frame into
 * the main database, and leaves the WAL empty. After it returns, the `.db` file
 * alone is a complete and consistent copy.
 *
 * ## Why it never throws
 *
 * This runs during backup, where the alternative to a slightly stale backup is
 * *no backup at all*. A checkpoint can legitimately fail — most often
 * `SQLITE_BUSY` when a long-running read holds the database — and that is not a
 * reason to abort the user's backup or crash their app. Every failure is
 * reported as a value, logged, and swallowed.
 */
@Singleton
class BackupCoordinator @Inject constructor(
    private val database: AppDatabase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /** Outcome of a checkpoint attempt. Never an exception. */
    sealed interface Result {
        /** WAL fully flushed. [framesCheckpointed] frames moved into the main DB. */
        data class Success(val framesCheckpointed: Int) : Result

        /**
         * SQLite ran the checkpoint but could not flush everything — almost
         * always a concurrent reader. The backup is still usable; it may just
         * be missing the most recent commits.
         */
        data class Partial(val framesCheckpointed: Int, val framesTotal: Int) : Result

        /** The checkpoint could not run. The backup proceeds regardless. */
        data class Failed(val cause: Throwable) : Result
    }

    /**
     * Runs `PRAGMA wal_checkpoint(FULL)` on [ioDispatcher].
     *
     * `wal_checkpoint` returns a row — `(busy, log_frames, checkpointed_frames)`
     * — so it must be issued as a *query*, not via `execSQL`. `execSQL` throws
     * "Queries can be performed using SQLiteDatabase query or rawQuery methods
     * only" on any statement that produces a result set. `DatabaseModule`
     * already carries a comment about this exact trap for `journal_mode`; the
     * same rule applies here.
     */
    suspend fun checkpoint(): Result = withContext(ioDispatcher) {
        runCatching {
            database.openHelper.writableDatabase
                .query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
                .use { cursor ->
                    if (!cursor.moveToFirst()) {
                        // No row is not documented behaviour, but treat it as a
                        // no-op rather than inventing a frame count.
                        return@runCatching Result.Success(framesCheckpointed = 0)
                    }
                    // Column 0 is the busy flag: 1 means SQLite could not get
                    // the lock it needed and gave up early.
                    val busy = cursor.getInt(0)
                    val logFrames = cursor.getInt(1)
                    val checkpointedFrames = cursor.getInt(2)

                    when {
                        busy == 0 && checkpointedFrames >= logFrames ->
                            Result.Success(checkpointedFrames)
                        else ->
                            Result.Partial(checkpointedFrames, logFrames)
                    }
                }
        }.getOrElse { throwable ->
            // Deliberately broad: this must not be able to take down a backup
            // pass or the app process, whatever SQLite decides to raise.
            Timber.w(throwable, "WAL checkpoint failed; backup will use the database as-is")
            Result.Failed(throwable)
        }.also { result ->
            when (result) {
                is Result.Success ->
                    Timber.d("WAL checkpoint complete (%d frames)", result.framesCheckpointed)
                is Result.Partial ->
                    Timber.i(
                        "WAL checkpoint partial: %d of %d frames flushed",
                        result.framesCheckpointed, result.framesTotal
                    )
                is Result.Failed -> Unit // already logged above
            }
        }
    }
}
