package com.tpstream.player.models

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tpstream.player.ImageSaver

@Entity
class OfflineVideoInfo(
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
    var downloadState: OfflineVideoState? = null,
    var videoWidth: Int = 0,
    var videoHeight: Int = 0
)

internal fun OfflineVideoInfo.asVideoInfo():VideoInfo{
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
    transcodingStatus
)
}

enum class OfflineVideoState {
    PAUSE,
    DOWNLOADING,
    COMPLETE,
    FAILED
}

internal fun getOfflineVideoState(int:Int):OfflineVideoState?{
    return when(int){
        1 -> OfflineVideoState.PAUSE
        2 -> OfflineVideoState.DOWNLOADING
        3 -> OfflineVideoState.COMPLETE
        4 -> OfflineVideoState.FAILED
        else -> null
    }
}

fun OfflineVideoInfo.getLocalThumbnail(context: Context): Bitmap?{
    return ImageSaver(context).load(videoId)
}