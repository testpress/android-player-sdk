package com.tpstream.player.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: LocalVideo)

    @Query("DELETE FROM Video WHERE videoId=:videoID")
    suspend fun delete(videoID:String)

    @Query("SELECT * FROM Video")
    fun getAllVideo():List<LocalVideo>?

    @Query("SELECT * FROM Video WHERE videoId=:videoID")
    fun getVideoByVideoId(videoID:String): LocalVideo?

    @Query("SELECT * FROM Video WHERE url=:url")
    fun getVideoByUrl(url:String): LocalVideo?

    @Query("SELECT * FROM Video WHERE videoId=:videoId")
    fun getVideoById(videoId:String): LiveData<LocalVideo?>

    @Query("SELECT * FROM Video WHERE downloadState NOT null")
    fun getAllDownloadInLiveData():LiveData<List<LocalVideo>?>

    @Query("DELETE FROM Video")
    suspend fun deleteAll()

}