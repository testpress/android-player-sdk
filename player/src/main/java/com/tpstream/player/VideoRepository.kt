package com.tpstream.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.Video
import com.tpstream.player.models.getVideoState

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

}