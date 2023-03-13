package com.tpstream.player.models

import android.content.Context
import android.graphics.Bitmap
import com.tpstream.player.ImageSaver

data class DomainVideo(
    var videoId: String = "",
    var title: String = "",
    var thumbnail: String = "",
    internal var url: String = "",
    var duration: String = "",
    var description: String = "",
    var transcodingStatus: String = "",
    var percentageDownloaded: Int = 0,
    var bytesDownloaded: Long = 0,
    var totalSize: Long = 0,
    var downloadState: DownloadStatus? = null,
    var videoWidth: Int = 0,
    var videoHeight: Int = 0
) {
    internal val isNotDownloaded get() = this.downloadState != DownloadStatus.COMPLETE

    fun getLocalThumbnail(context: Context): Bitmap?{
        return ImageSaver(context).load(videoId)
    }

    internal fun asDatabaseVideo():DatabaseVideo {
        return DatabaseVideo(
            videoId = this.videoId,
            title = this.title,
            thumbnail = this.thumbnail,
            url = this.url,
            duration = this.duration,
            description = this.description,
            transcodingStatus = this.transcodingStatus,
            percentageDownloaded = this.percentageDownloaded,
            bytesDownloaded = this.bytesDownloaded,
            totalSize = this.totalSize,
            downloadState = this.downloadState,
            videoWidth = this.videoWidth,
            videoHeight = this.videoHeight
        )
    }

    internal fun asNetworkVideo():NetworkVideo {
        return NetworkVideo(
            title,
            thumbnail,
            null,
            null,
            url,
            null,
            null,
            duration,
            description,
            transcodingStatus,
            videoId,
            null,
            null,
            null
        )
    }
}
