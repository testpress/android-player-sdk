package com.tpstream.player


import android.content.Context
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.models.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadTask private constructor(val context: Context) {

    private lateinit var url : String
    private lateinit var videoInfo: VideoInfo
    private lateinit var tpInitParams: TpInitParams
    private var trackSelector: DefaultTrackSelector
    private var override: MutableMap<TrackGroup, TrackSelectionOverride>

    internal constructor(url: String, context: Context) : this(context) {
        this.url = url
    }

    constructor(tpInitParams: TpInitParams,context: Context):this(context){
        this.tpInitParams = tpInitParams
        val urlUrl = "/api/v2.5/video_info/C3XLe1CCcOq/?access_token=c381512b-7337-4d8e-a8cf-880f4f08fd08"
        Network<VideoInfo>(tpInitParams.orgCode).get(urlUrl,object :Network.TPResponse<VideoInfo>{
            override fun onSuccess(result: VideoInfo) {
                this@DownloadTask.videoInfo = result
                VideoDownloadRequestCreationHandler(this@DownloadTask.context,videoInfo,this@DownloadTask.tpInitParams)
            }

            override fun onFailure(exception: TPException) {
                TODO("Not yet implemented")
            }

        })
    }

    init {
        trackSelector = DefaultTrackSelector(context)
        override = trackSelector.parameters.overrides.toMutableMap()

    }

    private val downloadManager = VideoDownloadManager(context).get()
    private val downloadIndex = downloadManager.downloadIndex


    fun start(downloadQuality:Int){
        Thread.sleep(5000)
        start(VideoDownloadRequestCreationHandler(
            context,
            videoInfo,
            tpInitParams
        ).buildDownloadRequest(override))

    }


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