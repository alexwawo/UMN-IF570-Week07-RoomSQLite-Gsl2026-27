package org.umn.week07demo

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.umn.week07demo.data.local.AppDatabase
import org.umn.week07demo.data.local.MIGRATION_2_3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2_keepsData_andDefaultsNotPinned() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL("INSERT INTO notes (id, title, content, createdAt) VALUES (1, 'T', 'C', 1000)")
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 2, true)

        db.query("SELECT title, isPinned FROM notes WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("T", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
        }
    }

    @Test
    fun migrate2To3_backfillsUpdatedAtFromCreatedAt() {
        helper.createDatabase(testDbName, 2).apply {
            execSQL(
                "INSERT INTO notes (id, title, content, createdAt, isPinned) " +
                        "VALUES (1, 'T', 'C', 1000, 0)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 3, true, MIGRATION_2_3)

        db.query("SELECT updatedAt FROM notes WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1000L, cursor.getLong(0))
        }
    }

    @Test
    fun migrateAll_1To3() {
        helper.createDatabase(testDbName, 1).close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        Room.databaseBuilder(context, AppDatabase::class.java, testDbName)
            .addMigrations(MIGRATION_2_3)
            .build()
            .apply {
                openHelper.writableDatabase.close()
                close()
            }
    }
}