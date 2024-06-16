package com.tpstream.player.data.source.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoomMigration4To5 {

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(createNewTable())
            database.execSQL(copyDataFromOldToNewTable())
            database.execSQL(deleteOldTable())
            database.execSQL(renameTable())
        }
    }

    private fun createNewTable(): String {
        return "CREATE TABLE AssetTemp (" +
                "`videoId` TEXT PRIMARY KEY NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`thumbnail` TEXT NOT NULL, " +
                "`url` TEXT NOT NULL, " +
                "`duration` TEXT NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`transcodingStatus` TEXT NOT NULL, " +
                "`percentageDownloaded` INTEGER NOT NULL, " +
                "`bytesDownloaded` INTEGER NOT NULL, " +
                "`totalSize` INTEGER NOT NULL, " +
                "`downloadState` TEXT, " +
                "`videoWidth` INTEGER NOT NULL, " +
                "`videoHeight` INTEGER NOT NULL)"
    }

    private fun copyDataFromOldToNewTable(): String {
        return "INSERT INTO AssetTemp " +
                "(videoId, " +
                "title, " +
                "thumbnail, " +
                "url, " +
                "duration, " +
                "description, " +
                "transcodingStatus, " +
                "percentageDownloaded, " +
                "bytesDownloaded, " +
                "totalSize, " +
                "downloadState, " +
                "videoWidth, " +
                "videoHeight) " +
                "SELECT " +
                "videoId, " +
                "title, " +
                "thumbnail, " +
                "url, " +
                "duration, " +
                "description, " +
                "transcodingStatus, " +
                "percentageDownloaded, " +
                "bytesDownloaded, " +
                "totalSize, " +
                "downloadState, " +
                "videoWidth, " +
                "videoHeight FROM Asset"
    }

    private fun deleteOldTable(): String {
        return "DROP TABLE Asset"
    }

    private fun renameTable(): String {
        return "ALTER TABLE AssetTemp RENAME TO Asset"
    }
}
