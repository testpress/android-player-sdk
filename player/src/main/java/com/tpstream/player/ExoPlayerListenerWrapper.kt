package com.tpstream.player

import androidx.media3.common.*

internal class ExoPlayerListenerWrapper(var player: TpStreamPlayerImpl) : Player.Listener {
    private val TAG = "ExoPlayerListener"
    var listener:TPStreamPlayerListener? = player._listener

    override fun onPlaybackStateChanged(playbackState: Int) {
        listener?.onPlaybackStateChanged(playbackState)
    }

    override fun onPlayerError(error: PlaybackException) {
        listener?.onPlayerError(error)
    }

    override fun onTracksChanged(tracks: Tracks) {
        listener?.onTracksChanged(tracks)
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        listener?.onPlayWhenReadyChanged(playWhenReady, reason)
    }

    override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
        listener?.onDeviceInfoChanged(deviceInfo)
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        listener?.onIsLoadingChanged(isLoading)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        listener?.onIsPlayingChanged(isPlaying)
    }

    override fun onMetadata(metadata: Metadata) {
        listener?.onMetadata(metadata)
    }

    override fun onEvents(exoplayer: Player, events: Player.Events) {
        listener?.onEvents(player, events)
    }

    override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
        listener?.onSeekBackIncrementChanged(seekBackIncrementMs)
    }

    override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
        listener?.onSeekForwardIncrementChanged(seekForwardIncrementMs)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        listener?.onVideoSizeChanged(videoSize)
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        listener?.onPositionDiscontinuity(oldPosition, newPosition, reason)
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        listener?.onPlayerErrorChanged(error)
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        listener?.onTimelineChanged(timeline, reason)
    }
}