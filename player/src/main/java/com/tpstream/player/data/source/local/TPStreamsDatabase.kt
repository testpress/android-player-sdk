package com.tpstream.player.data.source.local

import android.content.Context
import androidx.room.*
import com.tpstream.player.data.source.local.migration.RoomMigration1To2.MIGRATION_1_2
import com.tpstream.player.data.source.local.migration.RoomMigration2To3.MIGRATION_2_3
import com.tpstream.player.data.source.local.migration.RoomMigration3To4.MIGRATION_3_4
import com.tpstream.player.data.source.local.migration.RoomMigration4To5.MIGRATION_4_5
import com.tpstream.player.data.source.local.migration.RoomMigration5To6.MIGRATION_5_6
import com.tpstream.player.data.source.local.migration.RoomMigration6To7.MIGRATION_6_7

@Database(
    version = 7,
    entities = [LocalAsset::class],
    exportSchema = true
)
internal abstract class TPStreamsDatabase : RoomDatabase() {

    abstract fun assetDao(): AssetDao

    companion object {
        private lateinit var INSTANCE: TPStreamsDatabase

        private val MIGRATIONS = arrayOf(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7)

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