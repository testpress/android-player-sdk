package com.tpstream.player.data

import com.tpstream.player.data.source.local.LocalAsset
import com.tpstream.player.data.source.network.NetworkAsset
import com.tpstream.player.data.source.network.TestpressNetworkAsset
import com.tpstream.player.util.parseDateTime

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
            isDrmProtected = this.url.isDrmProtected()
        ),
        folderTree = this.folderTree,
        downloadStartTimeMs = this.downloadStartTimeMs,
        metadata = this.metadata
    )
}

internal fun List<LocalAsset>.asDomainAssets() = map(LocalAsset::asDomainAsset)

//NetworkVideo to Video
internal fun NetworkAsset.asDomainAsset(): Asset {
    val transcodingStatus = if (networkVideo != null) networkVideo.status else this.transcodingStatus
    val thumbnailUrl = if (networkVideo != null) networkVideo.preview_thumbnail_url
        ?: "" else thumbnail ?: ""
    val url = if (networkVideo != null) {
        if (networkVideo.isDrmProtected == true) networkVideo.dash_url
            ?: "" else networkVideo.playback_url ?: ""
    } else {
        dashUrl ?: url ?: ""
    }
    return Asset(
        id = this.id ?: "",
        type = this.type ?: "video",
        title = this.title ?: "",
        thumbnail = thumbnailUrl,
        video = Video(
            url = url,
            duration = networkVideo?.duration ?: 0,
            transcodingStatus = transcodingStatus ?: "",
            tracks = this.networkVideo?.tracks?.map {
                it.asDomainTracks()
            },
            isDrmProtected = networkVideo?.isDrmProtected
        ),
        description = description ?: "",
        liveStream = getDomainLiveStream(this),
        folderTree = folderTree
    )
}

internal fun getDomainLiveStream(asset: NetworkAsset): LiveStream? =
    if (asset.type == "livestream" && asset.networkLiveStream != null) {
        asset.networkLiveStream.run {
            LiveStream(
                url = url,
                status = status,
                startTime = parseDateTime(startTime),
                recordingEnabled = recordingEnabled,
                enabledDRMForLive = enabledDRMForLive,
                enabledDRMForRecording = enabledDRMForRecording,
                noticeMessage = noticeMessage
            )
        }
    } else {
        null
    }

internal fun NetworkAsset.NetworkVideo.Track.asDomainTracks(): Track {
    return Track(
        type = this.type ?: "",
        name = this.name ?: "",
        url = this.url ?: "",
        language = this.language ?: "",
        duration = this.duration ?: 0
    )
}


// TestpressNetworkAsset to Asset
internal fun TestpressNetworkAsset.asDomainAsset(): Asset {

    fun getLivestream(): LiveStream? {
        return if (this.networkLiveStream == null) {
            null
        } else {
            LiveStream(
                url = this.networkLiveStream.url,
                status = this.networkLiveStream.status ?: "",
                startTime = null,
                recordingEnabled = this.networkLiveStream.recordingEnabled ?: false,
                enabledDRMForRecording = false, // this field is not need will remove in future until default value is false
                enabledDRMForLive = this.networkLiveStream.enabledDRMForLive ?: false,
                noticeMessage = this.networkLiveStream.noticeMessage ?: ""
            )
        }
    }

    return Asset(
        id = this.id ?: "",
        type = this.type ?: "",
        title = this.title ?: "",
        thumbnail = this.networkVideo?.thumbnail ?: "",
        video = Video(
            url = (if (this.networkVideo?.isDrmProtected == true) this.networkVideo.dashUrl else this.networkVideo?.playbackUrl)
                ?: "",
            duration = networkVideo?.duration ?: 0,
            transcodingStatus = this.networkVideo?.status ?: "",
            tracks = null,
            isDrmProtected = networkVideo?.isDrmProtected
        ),
        description = this.networkVideo?.description ?: "",
        liveStream = getLivestream(),
        folderTree = null
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
        videoHeight = this.video.height,
        folderTree = this.folderTree,
        downloadStartTimeMs = this.downloadStartTimeMs,
        metadata = this.metadata
    )
}

internal fun String.isDrmProtected() = this.contains(".mpd")
