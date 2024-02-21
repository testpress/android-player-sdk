package com.tpstream.player.data.source.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoomMigration3To4 {

    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE Video RENAME TO Asset")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Asset_videoId` ON `Asset` (`videoId`)")
        }
    }

}