package com.tpstream.player.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tpstream.player.database.dao.OfflineVideoInfoDao
import com.tpstream.player.models.OfflineVideoInfo

@Database(version = 1, entities = [OfflineVideoInfo::class], exportSchema = true)
abstract class TPStreamsDatabase : RoomDatabase() {

    abstract fun offlineVideoInfoDao():OfflineVideoInfoDao

    companion object {
        private lateinit var INSTANCE: TPStreamsDatabase

        operator fun invoke(context: Context): TPStreamsDatabase {
            synchronized(TPStreamsDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        TPStreamsDatabase::class.java, "tpStreams-database")
                        .build()
                }
            }
            return INSTANCE
        }
    }
}