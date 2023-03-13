package com.tpstream.player.models

import com.google.gson.annotations.SerializedName

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
    val videoDetails: VideoDetails?
) {

    inner class VideoDetails(
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
        val thumbnailUrl = if (videoDetails != null) videoDetails.preview_thumbnail_url?:"" else thumbnail?:""
        val url = if (videoDetails != null){
            if (videoDetails.enable_drm == true) videoDetails.dash_url?:"" else videoDetails.playback_url?:""
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