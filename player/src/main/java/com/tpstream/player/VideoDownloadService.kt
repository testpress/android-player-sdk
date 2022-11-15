package com.tpstream.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.*
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import java.lang.Exception

private const val JOB_ID = 1
private const val FOREGROUND_NOTIFICATION_ID = 1
private const val CHANNEL_ID = "download_channel"
private var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1

class VideoDownloadService:DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download,
    R.string.download_description
) , DownloadManager.Listener{

    private lateinit var notificationHelper: DownloadNotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper = DownloadNotificationHelper(this, CHANNEL_ID)
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
        val navigateToDownloadsActivity = getIntentForNavigateToDownloadsActivity()

        return notificationHelper.buildProgressNotification(
            applicationContext,
            R.drawable.ic_baseline_download_for_offline_24,
            navigateToDownloadsActivity,
            null,
            downloads
        )
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?
    ) {
        var notification: Notification? = null

        when (download.state) {
            Download.STATE_COMPLETED -> notification = getCompletedNotification()
            Download.STATE_FAILED -> notification = getFailedNotification()
        }

        //notification?.contentIntent = getIntentForNavigateToDownloadsActivity()
        NotificationUtil.setNotification(this, nextNotificationId, notification)
    }

    private fun getIntentForNavigateToDownloadsActivity(): PendingIntent {
        val navigateToDownloadsActivity = Intent(this, DownloadsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this, 0, navigateToDownloadsActivity, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this, 0, navigateToDownloadsActivity, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
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


}