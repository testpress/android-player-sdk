package com.tpstream.player

import com.tpstream.player.enum.Error

interface TPStreamPlayerListener {
    fun onTracksChanged(tracks: Tracks) {}
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
    fun onPlayerErrorChanged(error: PlaybackException?) {}
    fun onTimelineChanged(timeline: Timeline, reason: Int) {}
    fun onPlaybackStateChanged(playbackState: Int) {}
    fun onPlayerError(error: Error) {}
    fun onMarkerCallback(timesInSeconds: Long) {}
    fun onFullScreenChanged(isFullScreen: Boolean) {}
}