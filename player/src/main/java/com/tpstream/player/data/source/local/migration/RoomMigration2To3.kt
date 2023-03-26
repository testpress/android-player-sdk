package com.tpstream.player.data.source.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoomMigration2To3 {

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE OfflineVideoInfo RENAME TO Video")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Video_videoId` ON `Video` (`videoId`)")
        }
    }

}