package com.tpstream.player.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tpstream.player.models.OfflineVideoInfo

@Dao
interface OfflineVideoInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offlineVideoInfo: OfflineVideoInfo)

    @Delete
    suspend fun delete(offlineVideoInfo: OfflineVideoInfo)

    @Query("SELECT * FROM OfflineVideoInfo")
    fun getAllOfflineVideoInfo():List<OfflineVideoInfo>?

    @Query("SELECT * FROM OfflineVideoInfo WHERE videoId=:videoID")
    fun getOfflineVideoInfoByVideoId(videoID:String): OfflineVideoInfo?

    @Query("SELECT * FROM OfflineVideoInfo WHERE dashUrl=:dashUrl")
    fun getOfflineVideoInfoByUrl(dashUrl:String): OfflineVideoInfo?

    @Query("SELECT * FROM OfflineVideoInfo WHERE videoId=:videoId")
    fun getOfflineVideoInfoById(videoId:String): LiveData<OfflineVideoInfo?>

}