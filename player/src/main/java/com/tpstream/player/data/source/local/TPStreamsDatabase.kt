package com.tpstream.player.data.source.local

import android.content.Context
import androidx.room.*
import com.tpstream.player.data.source.local.migration.RoomMigration1To2.MIGRATION_1_2
import com.tpstream.player.data.source.local.migration.RoomMigration2To3.MIGRATION_2_3

@Database(
    version = 3,
    entities = [LocalVideo::class],
    exportSchema = true
)
internal abstract class TPStreamsDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao

    companion object {
        private lateinit var INSTANCE: TPStreamsDatabase

        private val MIGRATIONS = arrayOf(MIGRATION_1_2,MIGRATION_2_3)

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