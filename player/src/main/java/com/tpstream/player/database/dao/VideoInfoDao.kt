package com.tpstream.player.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tpstream.player.models.VideoInfo

@Dao
interface VideoInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(videoInfo: VideoInfo)

    @Delete
    fun delete(videoInfo: VideoInfo)

    @Query("SELECT * FROM videoinfo WHERE url=:url")
    fun getByUrl(url: String): LiveData<VideoInfo?>

    @Query("SELECT * FROM videoinfo WHERE dashUrl=:dashUrl")
    fun getByDashUrl(dashUrl: String): LiveData<VideoInfo?>

    @Query("SELECT * FROM videoinfo ORDER BY title ASC")
    fun getAllVideoInfo():LiveData<List<VideoInfo>>

    @Query("SELECT * FROM videoinfo WHERE videoId=:videoID")
    fun getVideoUrlByVideoId(videoID:String):VideoInfo?


}