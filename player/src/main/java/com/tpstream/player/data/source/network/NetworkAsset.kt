package com.tpstream.player.data.source.network

import com.google.gson.annotations.SerializedName
import com.tpstream.player.data.Track

internal data class NetworkAsset(
    val title: String?,
    val thumbnail: String?,

    @SerializedName("thumbnail_small")
    val thumbnailSmall: String?,

    @SerializedName("thumbnail_medium")
    val thumbnailMedium: String?,
    val url: String?,

    @SerializedName("dash_url")
    val dashUrl: String?,

    @SerializedName("hls_url")
    val hlsUrl: String?,
    val duration: String?,
    val description: String?,

    @SerializedName("transcoding_status")
    val transcodingStatus: String?,
    val id: String?,
    val bytes: Long?,
    val type: String?,
    @SerializedName("video")
    val networkVideo: NetworkVideo?,

    @SerializedName("live_stream")
    val networkLiveStream: NetworkLiveStream?,

    @SerializedName("folder_tree")
    val folderTree: String?
) {

    inner class NetworkVideo(
        val progress: Int?,
        val thumbnails: Array<String>?,
        val status: String?,
        val playback_url: String?,
        val dash_url: String?,
        val preview_thumbnail_url: String?,
        val format: String?,
        val resolutions: Array<String>?,
        val video_codec: String?,
        val audio_codec: String?,
        @SerializedName("enable_drm")
        val isDrmProtected : Boolean?,
        val tracks: List<Track>?,
        val duration: Long?
    ) {
        inner class Track(
            val type: String?,
            val name: String?,
            val url: String?,
            val language: String?,
            val duration: Long?
        )
    }

    inner class NetworkLiveStream(
        val status: String,

        @SerializedName("start")
        val startTime: String,

        @SerializedName("hls_url")
        val url: String,

        @SerializedName("transcode_recorded_video")
        val recordingEnabled: Boolean,

        @SerializedName("enable_drm")
        val enabledDRMForLive: Boolean,

        @SerializedName("enable_drm_for_recording")
        val enabledDRMForRecording: Boolean,

        @SerializedName("notice_message")
        val noticeMessage: String?
    )
}