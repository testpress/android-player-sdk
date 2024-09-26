package com.tpstream.player.data.source.network

import com.google.gson.annotations.SerializedName

data class TPStreamsNetworkAsset(
    @SerializedName("title") val title: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("video") val video: Video?,
    @SerializedName("id") val id: String?,
    @SerializedName("live_stream") val liveStream: LiveStream?,
    @SerializedName("folder_tree") val folderTree: String?
) {
    data class Video(
        @SerializedName("status") val status: String?,
        @SerializedName("playback_url") val playbackUrl: String?,
        @SerializedName("dash_url") val dashUrl: String?,
        @SerializedName("preview_thumbnail_url") val previewThumbnailUrl: String?,
        @SerializedName("enable_drm") val enableDrm: Boolean?,
        @SerializedName("tracks") val tracks: List<Track>?,
        @SerializedName("duration") val duration: Long?,
    ) {
        data class Track(
            @SerializedName("id") val id: Int?,
            @SerializedName("type") val type: String?,
            @SerializedName("name") val name: String?,
            @SerializedName("url") val url: String?,
            @SerializedName("language") val language: String?,
            @SerializedName("duration") val duration: Long?,
            @SerializedName("playlists") val playlists: List<Playlist>?
        ) {
            data class Playlist(
                @SerializedName("name") val name: String?,
                @SerializedName("bytes") val bytes: Long?,
                @SerializedName("width") val width: Int?,
                @SerializedName("height") val height: Int?
            )
        }
    }

    data class LiveStream(
        @SerializedName("status") val status: String?,
        @SerializedName("hls_url") val hlsUrl: String?,
        @SerializedName("start") val start: String?,
        @SerializedName("transcode_recorded_video") val transcodeRecordedVideo: Boolean?,
        @SerializedName("enable_drm_for_recording") val enableDrmForRecording: Boolean?,
        @SerializedName("chat_embed_url") val chatEmbedUrl: String?,
        @SerializedName("enable_drm") val enableDrm: Boolean?,
        @SerializedName("notice_message") val noticeMessage: String?
    )
}