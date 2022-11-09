package com.tpstream.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import com.tpstream.player.models.DRMLicenseURL
import com.tpstream.player.models.VideoInfo

public interface TpStreamPlayer {
    abstract val params: TpInitParams
    fun load(parameters: TpInitParams)
    fun setPlayWhenReady(canPlay: Boolean)
    fun getPlayWhenReady(): Boolean
    fun getPlaybackState(): Int
    fun getCurrentTime(): Long
    fun getBufferedTime(): Long
    fun setPlaybackSpeed(speed: Float)
    fun seekTo(seconds: Long)
    fun release()
}

class TpStreamPlayerImpl(val player: ExoPlayer): TpStreamPlayer {
    override lateinit var params: TpInitParams

    private fun load(url: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    override fun load(parameters: TpInitParams) {
        params = parameters
        val url = "/api/v2.5/video_info/${parameters.videoId}/?access_token=${parameters.accessToken}"
        Network<VideoInfo>(parameters.orgCode).get(url, object : Network.TPResponse<VideoInfo> {
            override fun onSuccess(result: VideoInfo) {
                Log.d("TAG", "onSuccess: ")
                result.dashUrl?.let {
                    Handler(Looper.getMainLooper()).post {
                       load(it)
                    }
                }
            }

            override fun onFailure(exception: TPException) {
                if (exception.isNetworkError()){
                    Log.d("TAG", "isNetworkError: ${exception.response?.code}")
                } else if (exception.isClientError()){
                    Log.d("TAG", "isClientError: ${exception.response?.code}")
                }else if (exception.isUnauthenticated()){
                    Log.d("TAG", "isUnauthenticated: ${exception.response?.code}")
                }else if (exception.isServerError()){
                    Log.d("TAG", "isServerError: ${exception.response?.code}")
                }
            }
        })
    }

    override fun setPlayWhenReady(canPlay: Boolean) {
        player.playWhenReady = canPlay
    }

    override fun getPlayWhenReady() = player.playWhenReady
    override fun getPlaybackState(): Int = player.playbackState
    override fun getCurrentTime(): Long = player.currentPosition
    override fun getBufferedTime(): Long = player.bufferedPosition

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    override fun seekTo(seconds: Long) {
        player.seekTo(seconds)
    }

    override fun release() {
        player.release()
    }
}