package com.tpstream.player.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tpstream.player.models.OfflineVideoInfo

@Dao
internal interface OfflineVideoInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offlineVideoInfo: OfflineVideoInfo)

    @Delete
    suspend fun delete(offlineVideoInfo: OfflineVideoInfo)

    @Query("SELECT * FROM OfflineVideoInfo")
    fun getAllOfflineVideoInfo():List<OfflineVideoInfo>?

    @Query("SELECT * FROM OfflineVideoInfo WHERE videoId=:videoID")
    fun getOfflineVideoInfoByVideoId(videoID:String): OfflineVideoInfo?

    @Query("SELECT * FROM OfflineVideoInfo WHERE url=:url")
    fun getOfflineVideoInfoByUrl(url:String): OfflineVideoInfo?

    @Query("SELECT * FROM OfflineVideoInfo WHERE videoId=:videoId")
    fun getOfflineVideoInfoById(videoId:String): LiveData<OfflineVideoInfo?>

    @Query("SELECT * FROM OfflineVideoInfo")
    fun getAllDownloadInLiveData():LiveData<List<OfflineVideoInfo>?>


}