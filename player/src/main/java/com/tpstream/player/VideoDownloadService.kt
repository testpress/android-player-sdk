package com.tpstream.player

import android.app.Notification
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler

private const val JOB_ID = 1
private const val FOREGROUND_NOTIFICATION_ID = 1
private const val CHANNEL_ID = "download_channel"

class VideoDownloadService:DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download,
    R.string.download_description
) , DownloadManager.Listener{


    override fun getDownloadManager(): DownloadManager {
        TODO("Not yet implemented")
    }

    override fun getScheduler(): Scheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        TODO("Not yet implemented")
    }
}