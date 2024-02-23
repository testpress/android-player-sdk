package com.tpstream.player.data

import com.tpstream.player.data.source.local.LocalAsset
import com.tpstream.player.data.source.network.NetworkAsset

// LocalVideo to Video
internal fun LocalAsset.asDomainAsset(): Asset {
    return Asset(
        id = this.videoId,
        title = this.title,
        description = this.description,
        thumbnail = this.thumbnail,
        video = Video(
            url = this.url,
            duration = this.duration,
            transcodingStatus = this.transcodingStatus,
            percentageDownloaded = this.percentageDownloaded,
            bytesDownloaded = this.bytesDownloaded,
            totalSize = this.totalSize,
            downloadState = this.downloadState,
            width = this.videoWidth,
            height = this.videoHeight,
        )
    )
}

internal fun List<LocalAsset>.asDomainAssets() = map(LocalAsset::asDomainAsset)

//NetworkVideo to Video
internal fun NetworkAsset.asDomainAsset(): Asset {
    val transcodingStatus = if (networkVideo != null) networkVideo.status else this.transcodingStatus
    val thumbnailUrl = if (networkVideo != null) networkVideo.preview_thumbnail_url
        ?: "" else thumbnail ?: ""
    val url = if (networkVideo != null) {
        if (networkVideo.enable_drm == true) networkVideo.dash_url
            ?: "" else networkVideo.playback_url ?: ""
    } else {
        dashUrl ?: url ?: ""
    }
    return Asset(
        id = this.id ?: "",
        title = this.title ?: "",
        thumbnail = thumbnailUrl,
        video = Video(
            url = url,
            duration = duration ?: "",
            transcodingStatus = transcodingStatus ?: ""
        ),
        description = description ?: "",
    )
}

//Video to LocalVideo
internal fun Asset.asLocalAsset(): LocalAsset {
    return LocalAsset(
        videoId = this.id,
        title = this.title,
        thumbnail = this.thumbnail,
        url = this.video.url,
        duration = this.video.duration,
        description = this.description,
        transcodingStatus = this.video.transcodingStatus,
        percentageDownloaded = this.video.percentageDownloaded,
        bytesDownloaded = this.video.bytesDownloaded,
        totalSize = this.video.totalSize,
        downloadState = this.video.downloadState,
        videoWidth = this.video.width,
        videoHeight = this.video.height
    )
}
