package com.tpstream.player.data

import com.tpstream.player.data.source.local.LocalVideo
import com.tpstream.player.data.source.network.NetworkAsset

// LocalVideo to Video
internal fun LocalVideo.asDomainVideo(): Video {
    return Video(
        id = this.id,
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

internal fun List<LocalVideo>.asDomainVideos() = map(LocalVideo::asDomainVideo)

//NetworkVideo to Video
internal fun NetworkAsset.asDomainVideo(): Video {
    val thumbnailUrl = if (networkVideo != null) networkVideo.preview_thumbnail_url
        ?: "" else thumbnail ?: ""
    val url = if (networkVideo != null) {
        if (networkVideo.enable_drm == true) networkVideo.dash_url
            ?: "" else networkVideo.playback_url ?: ""
    } else {
        dashUrl ?: url ?: ""
    }
    return Video(
        videoId = this.id ?: "",
        title = this.title ?: "",
        thumbnail = thumbnailUrl,
        url = url,
        duration = duration ?: "",
        description = description ?: "",
        transcodingStatus = transcodingStatus ?: ""
    )
}

//Video to LocalVideo
internal fun Video.asLocalVideo(): LocalVideo {
    return LocalVideo(
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
