package com.tpstream.player.data

import android.content.Context
import android.graphics.Bitmap
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.data.source.local.DownloadStatus

data class Asset(
    var id: String = "",
    var title: String = "",
    var thumbnail: String = "",
    var description: String = "",
    var video: Video = Video()
) {
    fun getLocalThumbnail(context: Context): Bitmap?{
        return ImageSaver(context).load(id)
    }
}

data class Video(
    internal var url: String = "",
    var width: Int = 0,
    var height: Int = 0,
    var transcodingStatus: String = "",
    var duration: String = "",
    var percentageDownloaded: Int = 0,
    var bytesDownloaded: Long = 0,
    var totalSize: Long = 0,
    var downloadState: DownloadStatus? = null,
)