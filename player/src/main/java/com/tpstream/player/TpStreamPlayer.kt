package com.tpstream.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
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
    fun getCurrentResolutionEnum(): ResolutionOptions
    fun getCurrentResolution(): Int
}

class TpStreamPlayerImpl(val player: ExoPlayer,val context: Context): TpStreamPlayer {
    override lateinit var params: TpInitParams
    override lateinit var videoInfo: VideoInfo
    var currentResolutionOption = ResolutionOptions.AUTO

    private fun load(url: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            //.setMimeType(MimeTypes.APPLICATION_MPD)
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    override fun load(parameters: TpInitParams) {
        params = parameters
        val url = "/api/v2.5/video_info/${parameters.videoId}/?access_token=${parameters.accessToken}"
        Network<VideoInfo>(parameters.orgCode).get(url, object : Network.TPResponse<VideoInfo> {
            override fun onSuccess(result: VideoInfo) {
                videoInfo = result
                result.url?.let {
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

    override fun getCurrentResolutionEnum(): ResolutionOptions {
        return currentResolutionOption
    }

    override fun getCurrentResolution(): Int {
        Log.d("TAG", "getCurrentResolution: ${player.currentTracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }[0]}")
        return player.currentTracks.groups
            .filter { it.type == C.TRACK_TYPE_VIDEO }[0].getTrackFormat(0).width

    }

    override fun getVideoFormat(): Format? {
        return player.videoFormat
    }

    override fun getCurrentTrackGroups(): ImmutableList<Tracks.Group> {
        return player.currentTracks.groups
    }
}