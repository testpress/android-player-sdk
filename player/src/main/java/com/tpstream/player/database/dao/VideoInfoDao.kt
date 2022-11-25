package com.tpstream.player.database.dao

import androidx.room.*
import com.tpstream.player.models.VideoInfo

@Dao
interface VideoInfoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(videoInfo: VideoInfo)

    @Delete
    fun delete(videoInfo: VideoInfo)

    @Query("SELECT * FROM videoinfo")
    fun getAllVideoInfo():List<VideoInfo>

    @Query("SELECT * FROM videoinfo WHERE videoId=:videoID")
    fun getVideoInfoByVideoId(videoID:String):VideoInfo?

}