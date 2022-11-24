package com.tpstream.player.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tpstream.player.database.dao.VideoInfoDao
import com.tpstream.player.models.VideoInfo

@Database(version = 1, entities = [VideoInfo::class], exportSchema = true)
abstract class TPStreamsDatabase : RoomDatabase() {

    abstract fun videoInfoDao():VideoInfoDao

    companion object {
        private lateinit var INSTANCE: TPStreamsDatabase

        operator fun invoke(context: Context): TPStreamsDatabase {
            synchronized(TPStreamsDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        TPStreamsDatabase::class.java, "tpStreams-database")
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}