package com.tpstream.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer

public interface TpStreamPlayer {
    fun load(url: String)
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
    override fun load(url: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
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