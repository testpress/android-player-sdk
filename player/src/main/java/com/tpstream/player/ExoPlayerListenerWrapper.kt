package com.tpstream.player

internal class ExoPlayerListenerWrapper(var player: TpStreamPlayerImpl) : PlayerListener {
    private val TAG = "ExoPlayerListener"
    var listener:TPStreamPlayerListener? = player._listener

    override fun onPlaybackStateChanged(playbackState: Int) {
        listener?.onPlaybackStateChanged(playbackState)
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

    override fun onEvents(exoplayer: Player, events: PlayerEvents) {
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
        oldPosition: PlayerPositionInfo,
        newPosition: PlayerPositionInfo,
        reason: Int
    ) {
        listener?.onPositionDiscontinuity(oldPosition, newPosition, reason)
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        listener?.onTimelineChanged(timeline, reason)
    }
}