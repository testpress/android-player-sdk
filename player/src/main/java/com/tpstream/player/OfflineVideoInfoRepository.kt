package com.tpstream.player

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.OfflineVideoInfo

class OfflineVideoInfoRepository(context: Context) {

    private val offlineVideoInfoDao = TPStreamsDatabase(context).offlineVideoInfoDao()

    fun updateDownloadStatus(download: Download) {
        val offlineVideoInfo = offlineVideoInfoDao.getOfflineVideoInfoByUrl(download.request.uri.toString())
        offlineVideoInfo?.let {
            offlineVideoInfo.percentageDownloaded = download.percentDownloaded.toInt()
            offlineVideoInfo.bytesDownloaded = download.bytesDownloaded
            offlineVideoInfo.totalSize = download.contentLength
            offlineVideoInfoDao.insert(offlineVideoInfo)
        }
    }

    fun get(videoId: String): LiveData<OfflineVideoInfo?> {
        return offlineVideoInfoDao.getOfflineVideoInfoById(videoId)
    }

}