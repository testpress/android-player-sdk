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
    val transcodingStatus: String?
) {
    fun asOfflineVideoInfo():OfflineVideoInfo{
        return OfflineVideoInfo(
            title = title!!,
            thumbnail = thumbnail?:"",
            url = dashUrl?:url!!,
            duration = duration!!,
            transcodingStatus = transcodingStatus!!
        )
    }

    fun getPlaybackURL():String{
        return dashUrl ?: url ?: ""
    }
}