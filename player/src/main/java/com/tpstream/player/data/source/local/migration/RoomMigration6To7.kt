package com.tpstream.player.data.source.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoomMigration6To7 {

    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE Asset ADD COLUMN metadata TEXT")
        }
    }
}