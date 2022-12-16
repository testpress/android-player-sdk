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

    fun get(videoId: String): LiveData<OfflineVideoInfo?> {
        return offlineVideoInfoDao.getOfflineVideoInfoById(videoId)
    }

    private fun getOfflineVideoInfoByUrl(url: String):OfflineVideoInfo?{
        return if (isDash(url)){
            offlineVideoInfoDao.getOfflineVideoInfoByDashUrl(url)
        } else {
            offlineVideoInfoDao.getOfflineVideoInfoByUrl(url)
        }
    }

    fun getVideoIdByUrl(url:String):String? {
        return if (isDash(url)){
            offlineVideoInfoDao.getOfflineVideoInfoByDashUrl(url)?.videoId
        } else {
            offlineVideoInfoDao.getOfflineVideoInfoByUrl(url)?.videoId
        }
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

    private fun isDash(url: String):Boolean{
        return !url.contains(".m3u8")
    }

}