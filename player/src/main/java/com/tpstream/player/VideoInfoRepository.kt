package com.tpstream.player

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.media3.exoplayer.offline.Download
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.VideoInfo

class VideoInfoRepository(context: Context) {

    private val videoInfoDao = TPStreamsDatabase(context).videoInfoDao()

    fun updateDownloadStatus(download: Download) {
        val videoInfo = videoInfoDao.getVideoInfoByUrl(download.request.uri.toString())
        videoInfo?.let {
            videoInfo.percentageDownloaded = download.percentDownloaded.toInt()
            videoInfo.bytesDownloaded = download.bytesDownloaded
            videoInfo.totalSize = download.contentLength
            videoInfoDao.insert(videoInfo)
        }
    }

    fun get(videoId: String): LiveData<VideoInfo?> {
        return videoInfoDao.getVideoInfoById(videoId)
    }

}