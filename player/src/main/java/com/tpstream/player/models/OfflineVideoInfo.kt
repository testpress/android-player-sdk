package com.tpstream.player.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class OfflineVideoInfo(
    @PrimaryKey
    var videoId: String = "",
    var title: String = "",
    var thumbnail: String = "",
    var thumbnailSmall: String = "",
    var thumbnailMedium: String = "",
    var url: String = "",
    var dashUrl: String = "",
    var hlsUrl: String = "",
    var duration: String = "",
    var description: String = "",
    var transcodingStatus: String = "",
    var percentageDownloaded: Int = 0,
    var bytesDownloaded: Long = 0,
    var totalSize: Long = 0,
    var downloadState: OfflineVideoState? = null
)

internal fun OfflineVideoInfo.asVideoInfo():VideoInfo{
return VideoInfo(
    title,
    thumbnail,
    thumbnailSmall,
    thumbnailMedium,
    url,
    dashUrl,
    hlsUrl,
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