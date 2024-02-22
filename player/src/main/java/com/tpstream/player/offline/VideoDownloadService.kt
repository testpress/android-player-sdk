package com.tpstream.player.offline

import android.app.Notification
import com.tpstream.player.*
import com.tpstream.player.R
import com.tpstream.player.data.AssetRepository
import com.tpstream.player.data.source.local.getVideoState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception

private const val JOB_ID = 1
private const val FOREGROUND_NOTIFICATION_ID = 1
private const val CHANNEL_ID = "download_channel"
private var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1

internal class VideoDownloadService: DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download,
    R.string.download_description
) , DownloadManagerListener{

    private lateinit var notificationHelper: DownloadNotificationHelper
    private lateinit var assetRepository: AssetRepository
    private lateinit var downloadCallback : DownloadCallback

    override fun onCreate() {
        super.onCreate()
        assetRepository = AssetRepository(this)
        notificationHelper = DownloadNotificationHelper(this, CHANNEL_ID)
        downloadCallback = DownloadCallback.invoke()
    }


    override fun getDownloadManager(): DownloadManager {
        val downloadManager = VideoDownloadManager(this).get()
        downloadManager.addListener(this)
        return downloadManager
    }

    override fun getScheduler(): Scheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {

        refreshCurrentDownloadsStatus()

        return notificationHelper.buildProgressNotification(
            applicationContext,
            R.drawable.ic_baseline_download_for_offline_24,
            null,
            null,
            downloads,
            0
        )
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?
    ) {
        var notification: Notification? = null
        var videoId : String?

        runBlocking(Dispatchers.IO) {
            videoId = assetRepository.getAssetByUrl(download.request.uri.toString())?.id
        }

        when (download.state) {
            Download.STATE_COMPLETED ->{
                notification = getCompletedNotification()
                downloadCallback.onDownloadSuccess(videoId)
                updateDownloadStatus(download)
            }
            Download.STATE_FAILED -> notification = getFailedNotification()
            Download.STATE_STOPPED -> updateDownloadStatus(download)
            Download.STATE_DOWNLOADING -> updateDownloadStatus(download)
            Download.STATE_REMOVING -> updateDownloadStatus(download)
        }

        NotificationUtil.setNotification(this, nextNotificationId, notification)
    }

    private fun getFailedNotification(): Notification {
        val message = "Download is failed. Please try again"
        return notificationHelper.buildDownloadFailedNotification(
            applicationContext,
            R.drawable.ic_baseline_file_download_done_24,
            null,
            message
        )
    }

    private fun getCompletedNotification(): Notification {
        val message = "Download is completed"
        return notificationHelper.buildDownloadCompletedNotification(
            applicationContext,
            R.drawable.ic_baseline_file_download_done_24,
            null,
            message
        )
    }

    private fun refreshCurrentDownloadsStatus() {
        for (download in downloadManager.currentDownloads) {
            updateDownloadStatus(download)
        }
    }

    private fun updateDownloadStatus(download:Download){
        CoroutineScope(Dispatchers.IO).launch{
            val asset = assetRepository.getAssetByUrl(download.request.uri.toString())
            asset?.let {
                asset.video.percentageDownloaded = download.percentDownloaded.toInt()
                asset.video.bytesDownloaded = download.bytesDownloaded
                asset.video.totalSize = download.contentLength
                asset.video.downloadState = getVideoState(download.state)
                assetRepository.update(asset)
            }
        }
    }

}

internal class DownloadCallback private constructor(){

    var callback: Listener? = null

    fun onDownloadSuccess(videoId:String?) {
        callback?.onDownloadsSuccess(videoId)
    }

    companion object {

        private lateinit var INSTANCE: DownloadCallback

        operator fun invoke(): DownloadCallback {
            synchronized(DownloadCallback::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = DownloadCallback()
                }
                return INSTANCE
            }
        }
    }

    interface Listener {
        fun onDownloadsSuccess(videoId:String?)
    }
}