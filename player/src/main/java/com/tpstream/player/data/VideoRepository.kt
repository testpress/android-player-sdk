package com.tpstream.player.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.tpstream.player.BuildConfig
import com.tpstream.player.TPException
import com.tpstream.player.TpInitParams
import com.tpstream.player.data.source.local.TPStreamsDatabase
import com.tpstream.player.data.source.network.NetworkVideo
import com.tpstream.player.data.source.network.VideoNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        val url =BuildConfig.VIDEO_URL.format(params.orgCode,params.videoId,params.accessToken)
        VideoNetworkDataSource<NetworkVideo>().get(url, object : VideoNetworkDataSource.TPResponse<NetworkVideo> {
            override fun onSuccess(result: NetworkVideo) {
                val video = result.asDomainVideo()
                video.videoId = params.videoId!!
                storeVideo(video)
                callback.onSuccess(video)
            }

            override fun onFailure(exception: TPException) {
                callback.onFailure(exception)
            }
        })
    }

    private fun storeVideo(video: Video){
        CoroutineScope(Dispatchers.IO).launch {
            videoDao.insert(video.asLocalVideo())
        }
    }

}