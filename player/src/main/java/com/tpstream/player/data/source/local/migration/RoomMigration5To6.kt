package com.tpstream.player.data.source.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoomMigration5To6 {

    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(createNewTable())
            database.execSQL(copyDataFromOldToNewTable())
            database.execSQL(deleteOldTable())
            database.execSQL(renameTable())
        }
    }

    private fun createNewTable(): String {
        return """
            CREATE TABLE AssetNew (
                videoId TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                thumbnail TEXT NOT NULL,
                url TEXT NOT NULL,
                duration INTEGER NOT NULL,
                description TEXT NOT NULL,
                transcodingStatus TEXT NOT NULL,
                percentageDownloaded INTEGER NOT NULL,
                bytesDownloaded INTEGER NOT NULL,
                totalSize INTEGER NOT NULL,
                downloadState TEXT,
                videoWidth INTEGER NOT NULL,
                videoHeight INTEGER NOT NULL,
                folderTree TEXT,
                downloadStartTimeMs INTEGER NOT NULL
            )
        """.trimIndent()
    }

    private fun copyDataFromOldToNewTable(): String {
        return """
            INSERT INTO AssetNew (
                videoId,
                title,
                thumbnail,
                url,
                duration,
                description,
                transcodingStatus,
                percentageDownloaded,
                bytesDownloaded,
                totalSize,
                downloadState,
                videoWidth,
                videoHeight,
                folderTree,
                downloadStartTimeMs
            )
            SELECT 
                videoId,
                title,
                thumbnail,
                url,
                0 AS duration,
                description,
                transcodingStatus,
                percentageDownloaded,
                bytesDownloaded,
                totalSize,
                downloadState,
                videoWidth,
                videoHeight,
                NULL AS folderTree,
                ${System.currentTimeMillis()} AS downloadStartTimeMs
            FROM Asset
        """.trimIndent()
    }

    private fun deleteOldTable(): String {
        return "DROP TABLE Asset"
    }

    private fun renameTable(): String {
        return "ALTER TABLE AssetNew RENAME TO Asset"
    }
}