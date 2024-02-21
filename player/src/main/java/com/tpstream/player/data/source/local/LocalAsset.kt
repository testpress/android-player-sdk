package com.tpstream.player.data.source.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Asset", indices = [Index(value = ["videoId"], unique = true)])
internal class LocalAsset(
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
)

enum class DownloadStatus {
    PAUSE,
    DOWNLOADING,
    COMPLETE,
    FAILED
}

internal fun getVideoState(int:Int): DownloadStatus?{
    return when(int){
        1 -> DownloadStatus.PAUSE
        2 -> DownloadStatus.DOWNLOADING
        3 -> DownloadStatus.COMPLETE
        4 -> DownloadStatus.FAILED
        else -> null
    }
}