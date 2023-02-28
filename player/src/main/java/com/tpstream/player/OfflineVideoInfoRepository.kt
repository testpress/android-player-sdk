package com.tpstream.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.getOfflineVideoState

internal class OfflineVideoInfoRepository(context: Context) {

    private val offlineVideoInfoDao = TPStreamsDatabase(context).offlineVideoInfoDao()
    private val downloadManager = VideoDownloadManager(context).get()

    suspend fun refreshCurrentDownloadsStatus() {
        for (download in downloadManager.currentDownloads) {
            updateDownloadStatus(download)
        }
    }

    suspend fun updateDownloadStatus(download: Download) {
        val offlineVideoInfo = offlineVideoInfoDao.getOfflineVideoInfoByUrl(download.request.uri.toString())
        offlineVideoInfo?.let {
            offlineVideoInfo.percentageDownloaded = download.percentDownloaded.toInt()
            offlineVideoInfo.bytesDownloaded = download.bytesDownloaded
            offlineVideoInfo.totalSize = download.contentLength
            offlineVideoInfo.downloadState = getOfflineVideoState(download.state)
            offlineVideoInfoDao.insert(offlineVideoInfo)
        }
    }

    fun get(videoId: String): LiveData<OfflineVideoInfo?> {
        return offlineVideoInfoDao.getOfflineVideoInfoById(videoId)
    }

    fun grtVideoIdByUrl(url:String):String? {
        return offlineVideoInfoDao.getOfflineVideoInfoByUrl(url)?.videoId
    }

    suspend fun insert(offlineVideoInfo: OfflineVideoInfo){
        offlineVideoInfoDao.insert(offlineVideoInfo)
    }

    suspend fun delete(offlineVideoInfo: OfflineVideoInfo){
        offlineVideoInfoDao.delete(offlineVideoInfo)
    }

    fun getOfflineVideoInfoByVideoId(videoID:String): OfflineVideoInfo?{
        return offlineVideoInfoDao.getOfflineVideoInfoByVideoId(videoID)
    }

    fun getAllDownloadsInLiveData():LiveData<List<OfflineVideoInfo>?>{
        return offlineVideoInfoDao.getAllDownloadInLiveData()
    }

}