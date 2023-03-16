package com.tpstream.player.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tpstream.player.models.DatabaseVideo

@Dao
internal interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: DatabaseVideo)

    @Query("DELETE FROM Video WHERE videoId=:videoID")
    suspend fun delete(videoID:String)

    @Query("SELECT * FROM Video")
    fun getAllVideo():List<DatabaseVideo>?

    @Query("SELECT * FROM Video WHERE videoId=:videoID")
    fun getVideoByVideoId(videoID:String): DatabaseVideo?

    @Query("SELECT * FROM Video WHERE url=:url")
    fun getVideoByUrl(url:String): DatabaseVideo?

    @Query("SELECT * FROM Video WHERE videoId=:videoId")
    fun getVideoById(videoId:String): LiveData<DatabaseVideo?>

    @Query("SELECT * FROM Video WHERE downloadState NOT null")
    fun getAllDownloadInLiveData():LiveData<List<DatabaseVideo>?>

}