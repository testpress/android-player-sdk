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

    @Query("SELECT * FROM videoinfo")
    fun getAllVideoInfo():List<VideoInfo>?

    @Query("SELECT * FROM videoinfo WHERE videoId=:videoID")
    fun getVideoInfoByVideoId(videoID:String):VideoInfo?

    @Query("SELECT * FROM videoinfo WHERE dashUrl=:dashUrl")
    fun getVideoInfoByUrl(dashUrl:String): VideoInfo?

    @Query("SELECT * FROM videoinfo WHERE videoId=:videoId")
    fun getVideoInfoById(videoId:String): LiveData<VideoInfo?>

}