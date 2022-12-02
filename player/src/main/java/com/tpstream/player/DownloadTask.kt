package com.tpstream.player


import android.content.Context
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class DownloadTask (val context: Context) {

    private lateinit var url : String
    private lateinit var videoInfo: VideoInfo
    private lateinit var tpInitParams: TpInitParams
    private var trackSelector: DefaultTrackSelector
    private var override: MutableMap<TrackGroup, TrackSelectionOverride>

    constructor(url: String, context: Context) : this(context) {
        this.url = url
    }

    init {
        trackSelector = DefaultTrackSelector(context)
        override = trackSelector.parameters.overrides.toMutableMap()
    }

    private val downloadManager = VideoDownloadManager(context).get()
    private val downloadIndex = downloadManager.downloadIndex


    internal fun start(downloadRequest: DownloadRequest) {
        DownloadService.sendAddDownload(
            context,
            VideoDownloadService::class.java,
            downloadRequest,
            false
        )
    }

    fun pause() {
        val download = downloadIndex.getDownload(url)
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

    fun resume() {
        val download = downloadIndex.getDownload(url)
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

    fun delete() {
        val download = downloadIndex.getDownload(url)
        download?.let {
            DownloadService.sendRemoveDownload(
                context,
                VideoDownloadService::class.java,
                download.request.id,
                false
            )
        }
    }

    fun isDownloaded(): Boolean {
        val download = downloadIndex.getDownload(url)
        return download != null && download.state == Download.STATE_COMPLETED
    }

    fun isBeingDownloaded(): Boolean {
        val download = downloadIndex.getDownload(url)
        return download != null && download.state == Download.STATE_DOWNLOADING
    }

    fun getAllDownloads():List<OfflineVideoInfo>?{
        return runBlocking(Dispatchers.IO) {
            TPStreamsDatabase.invoke(context).offlineVideoInfoDao().getAllOfflineVideoInfo()
        }
    }

}

object VideoDownload {
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