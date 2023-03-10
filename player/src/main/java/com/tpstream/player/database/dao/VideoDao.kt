package com.tpstream.player.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tpstream.player.models.Video

@Dao
internal interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: Video)

    @Delete
    suspend fun delete(video: Video)

    @Query("SELECT * FROM OfflineVideoInfo")
    fun getAllVideo():List<Video>?

    @Query("SELECT * FROM OfflineVideoInfo WHERE videoId=:videoID")
    fun getVideoByVideoId(videoID:String): Video?

    @Query("SELECT * FROM OfflineVideoInfo WHERE url=:url")
    fun getVideoByUrl(url:String): Video?

    @Query("SELECT * FROM OfflineVideoInfo WHERE videoId=:videoId")
    fun getVideoById(videoId:String): LiveData<Video?>

    @Query("SELECT * FROM OfflineVideoInfo")
    fun getAllDownloadInLiveData():LiveData<List<Video>?>


}