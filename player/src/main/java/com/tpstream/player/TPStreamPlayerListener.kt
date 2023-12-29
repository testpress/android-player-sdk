package com.tpstream.player

import com.tpstream.player.enum.PlaybackError

interface TPStreamPlayerListener {
    fun onTracksChanged(tracks: Tracks) {}
    @Deprecated("Deprecated",level = DeprecationLevel.WARNING)
    fun onMetadata(metadata: Metadata) {}
    fun onIsPlayingChanged(playing: Boolean) {}
    fun onIsLoadingChanged(loading: Boolean) {}
    fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {}
    fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {}
    fun onEvents(player: TpStreamPlayer?, events: PlayerEvents) {}
    fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {}
    fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {}
    fun onVideoSizeChanged(videoSize: VideoSize) {}
    fun onPositionDiscontinuity(oldPosition: PlayerPositionInfo, newPosition: PlayerPositionInfo, reason: Int) {}
    @Deprecated("Deprecated", ReplaceWith("TPStreamPlayerListener.onPlayerError()"), DeprecationLevel.WARNING)
    fun onPlayerErrorChanged(error: PlaybackException?) {}
    fun onTimelineChanged(timeline: Timeline, reason: Int) {}
    fun onPlaybackStateChanged(playbackState: Int) {}
    fun onPlayerError(playbackError: PlaybackError) {}
    fun onMarkerCallback(timesInSeconds: Long) {}
    fun onFullScreenChanged(isFullScreen: Boolean) {}
}