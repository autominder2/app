package com.autominder.app.data.backup

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteQuery
import com.autominder.app.data.local.database.AppDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The checkpoint runs during backup, where the alternative to a stale backup is
 * no backup. These tests pin the two behaviours that matter: the busy/partial
 * signal is read correctly, and nothing SQLite can raise ever escapes.
 */
class BackupCoordinatorTest {

    private fun coordinatorReturning(cursor: Cursor): BackupCoordinator {
        val db = mockk<SupportSQLiteDatabase>()
        every { db.query(any<SupportSQLiteQuery>()) } returns cursor
        val helper = mockk<SupportSQLiteOpenHelper> { every { writableDatabase } returns db }
        val database = mockk<AppDatabase> { every { openHelper } returns helper }
        return BackupCoordinator(database, Dispatchers.Unconfined)
    }

    /** `PRAGMA wal_checkpoint` returns (busy, log_frames, checkpointed_frames). */
    private fun pragmaCursor(busy: Int, logFrames: Int, checkpointed: Int): Cursor =
        mockk<Cursor>(relaxed = true) {
            every { moveToFirst() } returns true
            every { getInt(0) } returns busy
            every { getInt(1) } returns logFrames
            every { getInt(2) } returns checkpointed
        }

    @Test
    fun `fully flushed WAL reports success with the frame count`() = runTest {
        val result = coordinatorReturning(pragmaCursor(busy = 0, logFrames = 42, checkpointed = 42))
            .checkpoint()

        assertEquals(BackupCoordinator.Result.Success(42), result)
    }

    @Test
    fun `empty WAL is success, not a failure`() = runTest {
        val result = coordinatorReturning(pragmaCursor(busy = 0, logFrames = 0, checkpointed = 0))
            .checkpoint()

        assertEquals(BackupCoordinator.Result.Success(0), result)
    }

    @Test
    fun `busy database reports partial so the caller knows records may be missing`() = runTest {
        val result = coordinatorReturning(pragmaCursor(busy = 1, logFrames = 100, checkpointed = 30))
            .checkpoint()

        assertEquals(BackupCoordinator.Result.Partial(30, 100), result)
    }

    @Test
    fun `unflushed frames report partial even when SQLite is not busy`() = runTest {
        val result = coordinatorReturning(pragmaCursor(busy = 0, logFrames = 100, checkpointed = 30))
            .checkpoint()

        assertEquals(BackupCoordinator.Result.Partial(30, 100), result)
    }

    @Test
    fun `a cursor with no row is treated as a no-op, not an invented count`() = runTest {
        val cursor = mockk<Cursor>(relaxed = true) { every { moveToFirst() } returns false }

        val result = coordinatorReturning(cursor).checkpoint()

        assertEquals(BackupCoordinator.Result.Success(0), result)
    }

    @Test
    fun `a throwing database never propagates - the backup must still proceed`() = runTest {
        val db = mockk<SupportSQLiteDatabase>()
        every { db.query(any<SupportSQLiteQuery>()) } throws IllegalStateException("database is locked")
        val helper = mockk<SupportSQLiteOpenHelper> { every { writableDatabase } returns db }
        val database = mockk<AppDatabase> { every { openHelper } returns helper }

        val result = BackupCoordinator(database, Dispatchers.Unconfined).checkpoint()

        assertTrue(result is BackupCoordinator.Result.Failed)
        assertEquals("database is locked", (result as BackupCoordinator.Result.Failed).cause.message)
    }
}
