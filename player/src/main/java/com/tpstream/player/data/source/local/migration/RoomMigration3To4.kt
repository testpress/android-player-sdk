package com.tpstream.player.data.source.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoomMigration3To4 {

    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE OfflineVideoInfo ADD COLUMN orgCode TEXT NOT NULL DEFAULT ''")
            database.execSQL("DELETE FROM Video WHERE downloadState IS NULL")
        }
    }

}