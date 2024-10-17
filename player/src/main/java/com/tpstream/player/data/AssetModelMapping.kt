package com.tpstream.player.data

import com.tpstream.player.data.source.local.LocalAsset
import com.tpstream.player.data.source.network.TPStreamsNetworkAsset
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

// TestpressNetworkAsset to Asset
internal fun TestpressNetworkAsset.asDomainAsset(): Asset {

    fun getPlayVideoPlayBackUrl(): String? {
        return if (this.video?.drmEnabled == true) {
            this.video.dashUrl
        } else {
            this.video?.url
        }
    }

    fun getVideo(): Video {
        return Video(
            url = getPlayVideoPlayBackUrl() ?: "",
            duration = video?.duration ?: 0,
            transcodingStatus = this.video?.transcodingStatus ?: "",
            tracks = null,
            isDrmProtected = video?.drmEnabled
        )
    }

    fun getLivestream(): LiveStream? {
        return if (this.liveStream == null) {
            null
        } else {
            LiveStream(
                url = this.liveStream.streamUrl ?: "",
                status = this.liveStream.status ?: "",
                startTime = null,
                recordingEnabled = this.liveStream.showRecordedVideo ?: false,
                enabledDRMForRecording = false, // this field is not need will remove in future until default value is false
                enabledDRMForLive = false,
                noticeMessage = this.liveStream.noticeMessage ?: ""
            )
        }
    }

    return Asset(
        id = this.id ?: "",
        type = this.contentType ?: "",
        title = this.title ?: "",
        thumbnail = this.video?.thumbnail ?: "",
        video = getVideo(),
        description = this.video?.description ?: "",
        liveStream = getLivestream(),
        folderTree = null
    )
}

// TPStreamsNetworkAsset to Asset
internal fun TPStreamsNetworkAsset.asDomainAsset(): Asset {

    fun getPlayVideoPlayBackUrl(): String? {
        return if (this.video?.hasH265Tracks == true) {
            val outputUrl = this.video.outputURLs?.get("h265")
            if (this.video.enableDrm == true) {
                outputUrl?.dashUrl
            } else {
                outputUrl?.hlsUrl
            }
        } else {
            if (this.video?.enableDrm == true) {
                this.video.dashUrl
            } else {
                this.video?.playbackUrl
            }
        }
    }

    fun TPStreamsNetworkAsset.Video.Track.getPlayLists(): List<Playlist> {
        if (this.playlists.isNullOrEmpty()) return listOf()
        return this.playlists.map { playlist ->
            Playlist(
                playlist.name ?: "",
                playlist.bytes ?: 0,
                playlist.width ?: 0,
                playlist.height ?: 0
            )
        }
    }

    fun getVideoTracks(): List<Track>? {
        return this.video?.tracks?.map { track ->
            Track(
                track.type ?: "",
                track.name ?: "",
                track.url ?: "",
                track.language ?: "",
                track.duration ?: 0,
                track.getPlayLists()
            )
        }
    }

    fun getOutputULRs(): Map<String, OutputUrl>? {
        val map = mutableMapOf<String, OutputUrl>()
        this.video?.outputURLs?.forEach {
            map[it.key] = OutputUrl(
                hlsUrl = it.value.hlsUrl,
                dashUrl = it.value.dashUrl
            )
        }
        return if (map.isEmpty()) null else map
    }

    fun getVideo(): Video {
        return Video(
            url = getPlayVideoPlayBackUrl() ?: "",
            duration = video?.duration ?: 0,
            transcodingStatus = this.video?.status ?: "",
            tracks = getVideoTracks(),
            isDrmProtected = video?.enableDrm,
            outputURLs = getOutputULRs()
        )
    }

    fun getLivestream(): LiveStream? {
        return if (this.liveStream == null) {
            null
        } else {
            LiveStream(
                url = this.liveStream.hlsUrl ?: "",
                status = this.liveStream.status ?: "",
                startTime = parseDateTime(this.liveStream.start ?: ""),
                recordingEnabled = this.liveStream.transcodeRecordedVideo ?: false,
                enabledDRMForRecording = this.liveStream.enableDrmForRecording ?: false,
                enabledDRMForLive = this.liveStream.enableDrm ?: false,
                noticeMessage = this.liveStream.noticeMessage ?: ""
            )
        }
    }

    return Asset(
        id = this.id ?: "",
        type = this.type ?: "",
        title = this.title ?: "",
        thumbnail = this.video?.previewThumbnailUrl ?: "",
        video = getVideo(),
        description = "",
        liveStream = getLivestream(),
        folderTree = this.folderTree
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
