package com.tpstream.player.data.source.network

import com.google.gson.annotations.SerializedName

internal data class TestpressNetworkAsset(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("content_type") val contentType: String?,
    @SerializedName("video") val video: Video?,
    @SerializedName("live_stream") val liveStream: LiveStream?
) {
    data class Video(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("thumbnail") val thumbnail: String?,
        @SerializedName("thumbnail_small") val thumbnailSmall: String?,
        @SerializedName("thumbnail_medium") val thumbnailMedium: String?,
        @SerializedName("url") val url: String?,
        @SerializedName("dash_url") val dashUrl: String?,
        @SerializedName("hls_url") val hlsUrl: String?,
        @SerializedName("duration") val duration: Long?,
        @SerializedName("description") val description: String?,
        @SerializedName("transcoding_status") val transcodingStatus: String?,
        @SerializedName("drm_enabled") val drmEnabled: Boolean?
    )

    data class LiveStream(
        @SerializedName("id")  val id: Long?,
        @SerializedName("title")  val title: String?,
        @SerializedName("stream_url") val streamUrl: String?,
        @SerializedName("duration")  val duration: Long?,
        @SerializedName("show_recorded_video") val showRecordedVideo: Boolean?,
        @SerializedName("status") val status: String?,
        @SerializedName("chat_embed_url") val chatEmbedUrl: String?,
        @SerializedName("notice_message") val noticeMessage: String?
    )
}
