package com.tpstream.player.data.source.network

import com.google.gson.annotations.SerializedName

internal data class TPStreamsNetworkAsset(
    val title: String?,
    val id: String?,
    val bytes: Long?,
    val type: String?,
    @SerializedName("folder_tree")
    val folderTree: String?,

    @SerializedName("video")
    val networkVideo: Video?,

    @SerializedName("live_stream")
    val networkLiveStream: LiveStream?
) {

    inner class Video(
        val status: String?,
        @SerializedName("playback_url")
        val playbackUrl: String?,
        @SerializedName("dash_url")
        val dashUrl: String?,
        @SerializedName("preview_thumbnail_url")
        val previewThumbnailUrl: String?,
        @SerializedName("enable_drm")
        val isDrmProtected: Boolean?,
        val tracks: List<Track>?,
        val duration: Long?
    ) {
        inner class Track(
            val type: String?,
            val name: String?,
            val url: String?,
            val language: String?,
            val duration: Long?,
        )
    }

    inner class LiveStream(
        val status: String,

        @SerializedName("hls_url")
        val url: String,

        @SerializedName("start")
        val startTime: String,

        @SerializedName("transcode_recorded_video")
        val recordingEnabled: Boolean,

        @SerializedName("enable_drm")
        val enabledDRMForLive: Boolean,

        @SerializedName("notice_message")
        val noticeMessage: String?
    )
}