package com.tpstream.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.Video
import com.tpstream.player.models.VideoInfo
import com.tpstream.player.models.asVideoInfo
import com.tpstream.player.models.getVideoState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class VideoRepository(val context: Context) {

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
        if (isVideoDownloadComplete(params, callback)) return
        fetchVideo(params, callback)
    }

    private fun isVideoDownloadComplete(params: TpInitParams,callback : Network.TPResponse<Video>):Boolean{
        var video : Video? = null
        var isDownloadComplete = false
        runBlocking(Dispatchers.IO) {
            video = videoDao.getVideoByVideoId(params.videoId!!)
            if (video != null) isDownloadComplete = DownloadTask(context).isDownloaded(video?.url!!)
        }
        return if (isDownloadComplete){
            callback.onSuccess(video!!)
            true
        } else {
            false
        }
    }

    private fun fetchVideo(
        params: TpInitParams,
        callback : Network.TPResponse<Video>
    ) {
        val url =
            "/api/v2.5/video_info/${params.videoId}/?access_token=${params.accessToken}"
        Network<VideoInfo>(params.orgCode).get(url, object : Network.TPResponse<VideoInfo> {
            override fun onSuccess(result: VideoInfo) {
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

    fun removeNotDownloadedVideo(){
        CoroutineScope(Dispatchers.IO).launch {
            videoDao.removeNotDownloaded()
        }
    }
}