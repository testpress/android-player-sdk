package com.tpstream.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadRequest
import com.google.common.collect.ImmutableList
import com.tpstream.player.models.VideoInfo

public interface TpStreamPlayer {
    abstract val params: TpInitParams
    val videoInfo : VideoInfo
    fun load(parameters: TpInitParams)
    fun setPlayWhenReady(canPlay: Boolean)
    fun getPlayWhenReady(): Boolean
    fun getPlaybackState(): Int
    fun getCurrentTime(): Long
    fun getBufferedTime(): Long
    fun setPlaybackSpeed(speed: Float)
    fun seekTo(seconds: Long)
    fun release()
    fun getVideoFormat(): Format?
    fun getCurrentTrackGroups(): ImmutableList<Tracks.Group>
    fun getDuration(): Long
}

class TpStreamPlayerImpl(val player: ExoPlayer,val context: Context): TpStreamPlayer {
    override lateinit var params: TpInitParams
    override lateinit var videoInfo: VideoInfo

    private fun load(url: String) {
        player.setMediaItem(getMediaItem(url))
        player.prepare()
    }

    internal fun getMediaItem(url:String):MediaItem {
        val downloadTask = DownloadTask(url, context)
        var mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setMultiSession(true)
                .build())
            .build()
        val downloadRequest: DownloadRequest? = VideoDownload.getDownloadRequest(url, context)
        Log.d("TAG", "getMediaItem: $url ${downloadTask.isDownloaded()}")
        if (downloadTask.isDownloaded() && downloadRequest != null) {
            val builder = mediaItem.buildUpon()
            builder
                .setMediaId(downloadRequest.id)
                .setUri(downloadRequest.uri)
                .setCustomCacheKey(downloadRequest.customCacheKey)
                .setMimeType(downloadRequest.mimeType)
                .setStreamKeys(downloadRequest.streamKeys)
                .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setKeySetId(downloadRequest.keySetId)
                    .build())

            mediaItem = builder.build()
        }
        return mediaItem
    }

    override fun load(parameters: TpInitParams) {
        params = parameters
        val url = "/api/v2.5/video_info/${parameters.videoId}/?access_token=${parameters.accessToken}"
        Network<VideoInfo>(parameters.orgCode).get(url, object : Network.TPResponse<VideoInfo> {
            override fun onSuccess(result: VideoInfo) {
                videoInfo = result
                result.dashUrl?.let {
                    Handler(Looper.getMainLooper()).post {
                        load(it)
                    }
                }
            }

            override fun onFailure(exception: TPException) {
                Handler(Looper.getMainLooper()).post {
                    load("https://verandademo-cdn.testpress.in/institute/demoveranda/courses/video-content/videos/transcoded/7e983f94530c4dadb2d4bed8b9e02f1e/video.mpd")
                }
                Log.d("TAG", "onFailure: ")
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
    override fun getDuration(): Long = player.duration

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    override fun seekTo(seconds: Long) {
        player.seekTo(seconds)
    }

    override fun release() {
        player.release()
    }

    override fun getVideoFormat(): Format? {
        return player.videoFormat
    }

    override fun getCurrentTrackGroups(): ImmutableList<Tracks.Group> {
        return player.currentTracks.groups
    }
}