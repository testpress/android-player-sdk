package com.tpstream.player.models

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tpstream.player.ImageSaver

@Entity(indices = [Index(value = ["videoId"], unique = true)])
class Video(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L,
    var videoId: String = "",
    var title: String = "",
    var thumbnail: String = "",
    var url: String = "",
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

}

internal fun Video.asVideoInfo():VideoInfo{
return VideoInfo(
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
    null
)
}

enum class DownloadStatus {
    PAUSE,
    DOWNLOADING,
    COMPLETE,
    FAILED
}

internal fun getVideoState(int:Int):DownloadStatus?{
    return when(int){
        1 -> DownloadStatus.PAUSE
        2 -> DownloadStatus.DOWNLOADING
        3 -> DownloadStatus.COMPLETE
        4 -> DownloadStatus.FAILED
        else -> null
    }
}

fun Video.getLocalThumbnail(context: Context): Bitmap?{
    return ImageSaver(context).load(videoId)
}