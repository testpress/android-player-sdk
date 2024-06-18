package com.tpstream.player.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
internal interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: LocalAsset)

    @Update()
    suspend fun update(asset: LocalAsset)

    @Query("DELETE FROM Asset WHERE videoId=:videoID")
    suspend fun delete(videoID:String)

    @Query("SELECT * FROM Asset")
    fun getAllAsset():List<LocalAsset>?

    @Query("SELECT * FROM Asset WHERE videoId=:videoID")
    fun getAssetByVideoId(videoID:String): LocalAsset?

    @Query("SELECT * FROM Asset WHERE url=:url")
    fun getAssetByUrl(url:String): LocalAsset?

    @Query("SELECT * FROM Asset WHERE videoId=:videoId")
    fun getAssetById(videoId:String): LiveData<LocalAsset?>

    @Query("SELECT * FROM Asset WHERE downloadState NOT null")
    fun getAllDownloadInLiveData():LiveData<List<LocalAsset>?>

    @Query("DELETE FROM Asset")
    suspend fun deleteAll()

}