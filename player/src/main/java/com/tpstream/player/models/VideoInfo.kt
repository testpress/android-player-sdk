package com.tpstream.player.models

import com.google.gson.annotations.SerializedName

data class VideoInfo(
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

    var video: Video?
) {

    inner class Video(
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

//    fun asVideo():com.tpstream.player.models.Video{
//        return com.tpstream.player.models.Video(
//            title = title!!,
//            thumbnail = thumbnail?:"",
//            url = dashUrl?:url!!,
//            duration = duration!!,
//            transcodingStatus = transcodingStatus!!
//        )
//    }

    fun asVideo():com.tpstream.player.models.Video{
        return com.tpstream.player.models.Video(
            title = this.title?:"",
            thumbnail = "",
            url = if (this.video != null){
                if (this.video?.enable_drm == true){
                    this.video?.dash_url?:""
                } else {
                    this.video?.playback_url?:""
                }
            } else {
                dashUrl?:url?:""
            },
            duration = "",
            description = "",
            transcodingStatus = ""
        )
    }

    fun getPlaybackURL():String{
        return dashUrl ?: url ?: ""
    }
}