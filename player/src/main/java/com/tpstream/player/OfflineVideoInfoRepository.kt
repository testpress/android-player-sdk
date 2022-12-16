package com.tpstream.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.getOfflineVideoState

class OfflineVideoInfoRepository(context: Context) {

    private val offlineVideoInfoDao = TPStreamsDatabase(context).offlineVideoInfoDao()

    suspend fun updateDownloadStatus(download: Download) {
        val offlineVideoInfo = getOfflineVideoInfoByUrl(download.request.uri.toString())
        offlineVideoInfo?.let {
            offlineVideoInfo.percentageDownloaded = download.percentDownloaded.toInt()
            offlineVideoInfo.bytesDownloaded = download.bytesDownloaded
            offlineVideoInfo.totalSize = download.contentLength
            offlineVideoInfo.downloadState = getOfflineVideoState(download.state)
            offlineVideoInfoDao.insert(offlineVideoInfo)
        }
    }

    private fun getOfflineVideoInfoByUrl(url:String):OfflineVideoInfo? {
        if (url.contains(".m3u8")){
            return offlineVideoInfoDao.getOfflineVideoInfoByUrl(url)
        }
        return offlineVideoInfoDao.getOfflineVideoInfoByDashUrl(url)
    }

    fun get(videoId: String): LiveData<OfflineVideoInfo?> {
        return offlineVideoInfoDao.getOfflineVideoInfoById(videoId)
    }

    fun getVideoIdByUrl(url:String):String? {
        if (url.contains(".m3u8")){
            return offlineVideoInfoDao.getOfflineVideoInfoByUrl(url)?.videoId
        }
        return offlineVideoInfoDao.getOfflineVideoInfoByDashUrl(url)?.videoId
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

}