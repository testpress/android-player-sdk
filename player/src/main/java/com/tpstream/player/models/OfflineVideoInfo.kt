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
)