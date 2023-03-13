package com.tpstream.player

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.Video
import com.tpstream.player.models.NetworkVideo
import com.tpstream.player.models.asVideoInfo
import com.tpstream.player.models.getVideoState
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

    fun get(videoId: String): LiveData<Video?> {
        return videoDao.getVideoById(videoId)
    }

    fun grtVideoIdByUrl(url:String):String? {
        return videoDao.getVideoByUrl(url)?.videoId
    }

    suspend fun insert(video: Video){
        videoDao.insert(video)
    }

    suspend fun delete(video: Video){
        videoDao.delete(video)
    }

    fun getVideoByVideoId(videoID:String): Video?{
        return videoDao.getVideoByVideoId(videoID)
    }

    fun getAllDownloadsInLiveData():LiveData<List<Video>?>{
        return videoDao.getAllDownloadInLiveData()
    }

    fun getVideo(
        params: TpInitParams,
        callback : Network.TPResponse<Video>
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
            video = videoDao.getVideoByVideoId(params.videoId!!)
        }
        return video
    }

    private fun fetchVideo(
        params: TpInitParams,
        callback : Network.TPResponse<Video>
    ) {
        val url = "https://${params.orgCode}.testpress.in/api/v2.5/video_info/${params.videoId}/?access_token=${params.accessToken}"
        Network<NetworkVideo>().get(url, object : Network.TPResponse<NetworkVideo> {
            override fun onSuccess(result: NetworkVideo) {
                val video = result.asVideo()
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
            videoDao.insert(video)
        }
    }

}