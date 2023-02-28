package com.tpstream.player


import android.content.Context
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.OfflineVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

internal class DownloadTask (val context: Context) {

    private val downloadManager = VideoDownloadManager(context).get()
    private val downloadIndex = downloadManager.downloadIndex

    fun start(downloadRequest: DownloadRequest) {
        DownloadService.sendAddDownload(
            context,
            VideoDownloadService::class.java,
            downloadRequest,
            false
        )
    }

    fun pause(info:OfflineVideoInfo) {
        val download = downloadIndex.getDownload(info.url)
        val STOP_REASON_PAUSED = 1
        download?.let {
            DownloadService.sendSetStopReason(
                context,
                VideoDownloadService::class.java,
                download.request.id,
                STOP_REASON_PAUSED,
                false
            )
        }
    }

    fun resume(info:OfflineVideoInfo) {
        val download = downloadIndex.getDownload(info.url)
        download?.let {
            DownloadService.sendSetStopReason(
                context,
                VideoDownloadService::class.java,
                download.request.id,
                Download.STOP_REASON_NONE,
                false
            )
        }
    }

    fun delete(info:OfflineVideoInfo) {
        val download = downloadIndex.getDownload(info.url)
        download?.let {
            DownloadService.sendRemoveDownload(
                context,
                VideoDownloadService::class.java,
                download.request.id,
                false
            )
        }
    }

    fun isDownloaded(url:String): Boolean {
        val download = downloadIndex.getDownload(url)
        return download != null && download.state == Download.STATE_COMPLETED
    }
}

internal object VideoDownload {
    @JvmStatic
    fun getDownloadRequest(url: String, context: Context): DownloadRequest? {
        val downloadManager = VideoDownloadManager(context).get()
        val downloadIndex = downloadManager.downloadIndex
        val download = downloadIndex.getDownload(url)
        return if (download != null && download.state != Download.STATE_FAILED) download.request else null
    }

    @JvmStatic
    fun getDownload(url: String, context: Context): Download? {
        val downloadManager = VideoDownloadManager(context).get()
        val downloadIndex = downloadManager.downloadIndex
        return downloadIndex.getDownload(url)
    }
}