package com.tpstream.player.models

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tpstream.player.ImageSaver

@Entity(tableName = "OfflineVideoInfo")
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
    var downloadState: VideoState? = null,
    var videoWidth: Int = 0,
    var videoHeight: Int = 0
)

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
    transcodingStatus
)
}

enum class VideoState {
    PAUSE,
    DOWNLOADING,
    COMPLETE,
    FAILED
}

internal fun getVideoState(int:Int):VideoState?{
    return when(int){
        1 -> VideoState.PAUSE
        2 -> VideoState.DOWNLOADING
        3 -> VideoState.COMPLETE
        4 -> VideoState.FAILED
        else -> null
    }
}

fun Video.getLocalThumbnail(context: Context): Bitmap?{
    return ImageSaver(context).load(videoId)
}