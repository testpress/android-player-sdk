package com.tpstream.player.models

data class Video(
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
    val enable_drm: Boolean?
)

data class TpStreamVideoInfo(
    val id: String?,
    val title: String?,
    val bytes: Long?,
    val type: String?,
    val video: Video?
) {

    fun asVideoInfo():VideoInfo{
        return VideoInfo(
            title = this.title,
            thumbnail = "",
            thumbnailSmall = "",
            thumbnailMedium = "",
            url = this.video?.playback_url,
            dashUrl = this.video?.dash_url,
            hlsUrl = "",
            duration = "",
            description = "",
            transcodingStatus = ""
        )
    }
}