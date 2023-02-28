package com.tpstream.player.database.roomMigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoomMigration1To2 {

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(createNewTable())
            database.execSQL(copyDataFromOldToNewTable())
            database.execSQL(deleteOldTable())
            database.execSQL(renameTable())
        }
    }

    private fun createNewTable(): String {
        return "CREATE TABLE OfflineVideoInfoTemp (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`videoId` TEXT NOT NULL, " +
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
        return "INSERT INTO OfflineVideoInfoTemp " +
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
                "videoHeight FROM OfflineVideoInfo"
    }

    fun deleteOldTable(): String {
        return "DROP TABLE OfflineVideoInfo"
    }

    fun renameTable(): String {
        return "ALTER TABLE OfflineVideoInfoTemp RENAME TO OfflineVideoInfo"
    }
}