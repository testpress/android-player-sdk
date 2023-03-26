package com.tpstream.player.data.source.network

import com.google.gson.annotations.SerializedName
import com.tpstream.player.data.Video

internal data class NetworkVideo(
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
    val networkVideoContent: NetworkVideoContent?
) {

    inner class NetworkVideoContent(
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

    fun asDomainVideo():Video {
        val thumbnailUrl = if (networkVideoContent != null) networkVideoContent.preview_thumbnail_url?:"" else thumbnail?:""
        val url = if (networkVideoContent != null){
            if (networkVideoContent.enable_drm == true) networkVideoContent.dash_url?:"" else networkVideoContent.playback_url?:""
        } else {
            dashUrl?:url?:""
        }
        return Video(
            videoId = this.id?:"",
            title = this.title?:"",
            thumbnail = thumbnailUrl,
            url = url,
            duration = duration?:"",
            description = description?:"",
            transcodingStatus = transcodingStatus?:""
        )
    }
}