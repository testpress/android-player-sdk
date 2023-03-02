package com.tpstream.player.database

import android.content.Context
import androidx.room.*
import com.tpstream.player.database.dao.OfflineVideoInfoDao
import com.tpstream.player.database.roomMigration.RoomMigration1To2.MIGRATION_1_2
import com.tpstream.player.database.roomMigration.RoomMigration2To3.MIGRATION_2_3
import com.tpstream.player.models.OfflineVideoInfo

@Database(
    version = 3,
    entities = [OfflineVideoInfo::class],
    exportSchema = true
)
internal abstract class TPStreamsDatabase : RoomDatabase() {

    abstract fun offlineVideoInfoDao(): OfflineVideoInfoDao

    companion object {
        private lateinit var INSTANCE: TPStreamsDatabase

        private val MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

        operator fun invoke(context: Context): TPStreamsDatabase {
            synchronized(TPStreamsDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        TPStreamsDatabase::class.java, "tpStreams-database"
                    ).addMigrations(*MIGRATIONS)
                        .build()
                }
            }
            return INSTANCE
        }
    }
}