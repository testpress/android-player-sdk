package com.tpstream.player.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: LocalAsset)

    @Query("DELETE FROM Asset WHERE videoId=:videoID")
    suspend fun delete(videoID:String)

    @Query("SELECT * FROM Asset")
    fun getAllVideo():List<LocalAsset>?

    @Query("SELECT * FROM Asset WHERE videoId=:videoID")
    fun getVideoByVideoId(videoID:String): LocalAsset?

    @Query("SELECT * FROM Asset WHERE url=:url")
    fun getVideoByUrl(url:String): LocalAsset?

    @Query("SELECT * FROM Asset WHERE videoId=:videoId")
    fun getVideoById(videoId:String): LiveData<LocalAsset?>

    @Query("SELECT * FROM Asset WHERE downloadState NOT null")
    fun getAllDownloadInLiveData():LiveData<List<LocalAsset>?>

    @Query("DELETE FROM Asset")
    suspend fun deleteAll()

}