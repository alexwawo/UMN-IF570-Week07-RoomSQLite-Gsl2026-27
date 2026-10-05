package org.umn.week07demo.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        // Backfill: nilai awal updatedAt = createdAt (tidak bisa dilakukan AutoMigration)
        db.execSQL("UPDATE notes SET updatedAt = createdAt")
    }
}