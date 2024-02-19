package com.tpstream.player.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.tpstream.player.TPException
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import com.tpstream.player.data.source.local.TPStreamsDatabase
import com.tpstream.player.data.source.network.NetworkAsset
import com.tpstream.player.data.source.network.VideoNetworkDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

internal class VideoRepository(context: Context) {

    private val videoDao = TPStreamsDatabase(context).videoDao()

    suspend fun update(video: Video){
        videoDao.insert(video.asLocalVideo())
    }

    fun get(videoId: String): LiveData<Video?> {
        return Transformations.map(videoDao.getVideoById(videoId)) {
            it?.asDomainVideo()
        }
    }

    fun getVideoByUrl(url:String):Video? {
        return videoDao.getVideoByUrl(url)?.asDomainVideo()
    }

    suspend fun insert(video: Video){
        videoDao.insert(video.asLocalVideo())
    }

    suspend fun delete(video: Video){
        videoDao.delete(video.videoId)
    }

    suspend fun deleteAll(){
        videoDao.deleteAll()
    }

    fun getAllDownloadsInLiveData():LiveData<List<Video>?>{
        return Transformations.map(videoDao.getAllDownloadInLiveData()) {
            it?.asDomainVideos()
        }
    }

    fun getVideo(
        params: TpInitParams,
        callback : VideoNetworkDataSource.TPResponse<Video>
    ){
        val video = getVideoFromDB(params)
        if (video != null) {
            callback.onSuccess(video)
        } else {
            fetchVideo(params, callback)
        }
    }

    private fun getVideoFromDB(params: TpInitParams): Video?{
        var video : Video? = null
        runBlocking(Dispatchers.IO) {
            video = videoDao.getVideoByVideoId(params.videoId!!)?.asDomainVideo()
        }
        return video
    }

    private fun fetchVideo(
        params: TpInitParams,
        callback : VideoNetworkDataSource.TPResponse<Video>
    ) {
        val url = TPStreamsSDK.constructVideoInfoUrl(params.videoId, params.accessToken)
        VideoNetworkDataSource<NetworkAsset>().get(url, object : VideoNetworkDataSource.TPResponse<NetworkAsset> {
            override fun onSuccess(result: NetworkAsset) {
                val video = result.asDomainVideo()
                video.videoId = params.videoId!!
                callback.onSuccess(video)
            }

            override fun onFailure(exception: TPException) {
                callback.onFailure(exception)
            }
        })
    }

}