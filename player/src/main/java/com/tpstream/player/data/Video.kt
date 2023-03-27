package com.tpstream.player.data

import android.content.Context
import android.graphics.Bitmap
import com.tpstream.player.ImageSaver
import com.tpstream.player.data.source.local.DownloadStatus

data class Video(
    internal var id:Long? = null,
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

}
