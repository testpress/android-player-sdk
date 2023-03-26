package com.tpstream.player

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.data.Video
import com.tpstream.player.data.source.local.LocalVideo
import com.tpstream.player.data.source.local.TPStreamsDatabase
import com.tpstream.player.models.*
import com.tpstream.player.data.source.network.NetworkVideo
import com.tpstream.player.data.source.local.getVideoState
import com.tpstream.player.data.source.network.VideoNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class VideoRepository(context: Context) {

    private val videoDao = TPStreamsDatabase(context).videoDao()
    private val downloadManager = VideoDownloadManager(context).get()

    suspend fun refreshCurrentDownloadsStatus() {
        for (download in downloadManager.currentDownloads) {
            updateDownloadStatus(download)
        }
    }

    suspend fun updateDownloadStatus(download: Download) {
        val video = videoDao.getVideoByUrl(download.request.uri.toString())
        video?.let {
            video.percentageDownloaded = download.percentDownloaded.toInt()
            video.bytesDownloaded = download.bytesDownloaded
            video.totalSize = download.contentLength
            video.downloadState = getVideoState(download.state)
            videoDao.insert(video)
        }
    }

    fun get(videoId: String): LiveData<LocalVideo?> {
        return videoDao.getVideoById(videoId)
    }

    fun grtVideoIdByUrl(url:String):String? {
        return videoDao.getVideoByUrl(url)?.videoId
    }

    suspend fun insert(video: LocalVideo){
        videoDao.insert(video)
    }

    suspend fun delete(video: LocalVideo){
        videoDao.delete(video.videoId)
    }

    fun getVideoByVideoId(videoID:String): LocalVideo?{
        return videoDao.getVideoByVideoId(videoID)
    }

    fun getAllDownloadsInLiveData():LiveData<List<LocalVideo>?>{
        return videoDao.getAllDownloadInLiveData()
    }

    fun getVideo(
        params: TpInitParams,
        callback : VideoNetworkDataSource.TPResponse<Video>
    ){
        val video = getVideoFromDB(params)
        if (video != null) {
            callback.onSuccess(video.asDomainVideo())
        } else {
            fetchVideo(params, callback)
        }
    }

    private fun getVideoFromDB(params: TpInitParams): LocalVideo?{
        var video : LocalVideo? = null
        runBlocking(Dispatchers.IO) {
            video = videoDao.getVideoByVideoId(params.videoId!!)
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