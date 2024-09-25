package com.tpstream.player.data.source.network

import com.google.gson.annotations.SerializedName

internal data class TestpressNetworkAsset(
    val title: String?,
    val id: String?,

    @SerializedName("content_type")
    val type: String?,

    @SerializedName("video")
    val networkVideo: Video?,

    @SerializedName("live_stream")
    val networkLiveStream: LiveStream?
) {

    inner class Video(
        @SerializedName("url")
        val playbackUrl: String?,

        @SerializedName("dash_url")
        val dashUrl: String?,
        val duration: Long?,
        val thumbnail: String?,
        val description:String?,

        @SerializedName("transcoding_status")
        val status: String?,

        @SerializedName("enable_drm")
        val isDrmProtected: Boolean?,
    )

    inner class LiveStream(
        @SerializedName("stream_url")
        val url: String,
        val duration: String?,

        @SerializedName("show_recorded_video")
        val recordingEnabled: Boolean,
        val status: String,

        @SerializedName("enable_drm")
        val enabledDRMForLive: Boolean,

        @SerializedName("notice_message")
        val noticeMessage: String?
    )
}
