package com.tpstream.player

import android.os.SystemClock
import android.text.TextUtils
import androidx.media3.common.*
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.Player.*
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Log
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import com.google.common.collect.ImmutableList
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TestpressLogger(private val tag: String = DEFAULT_TAG,val logTextCallback: (String) -> Unit) : AnalyticsListener {

    companion object {
        private val DEFAULT_TAG = "TestpressLogger"
    }

    private val MAX_TIMELINE_ITEM_LINES = 3
    private var startTimeMs: Long = 0
    private val TIME_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.US)
    private var window: androidx.media3.common.Timeline.Window? = null
    private var period: androidx.media3.common.Timeline.Period? = null
    val eventLogString: String get() = _eventLogString
    private var _eventLogString = ""

    init {
        TIME_FORMAT.minimumFractionDigits = 2
        TIME_FORMAT.maximumFractionDigits = 2
        TIME_FORMAT.isGroupingUsed = false
        window = androidx.media3.common.Timeline.Window()
        period = androidx.media3.common.Timeline.Period()
        startTimeMs = SystemClock.elapsedRealtime()
    }

    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
        logd(eventTime, "state", getStateString(state))
    }

    override fun onPlayWhenReadyChanged(eventTime: EventTime, playWhenReady: Boolean, reason: Int) {
        logd(
            eventTime,
            "playWhenReady",
            playWhenReady.toString() + ", " + getPlayWhenReadyChangeReasonString(reason)
        )
    }

    override fun onPlaybackSuppressionReasonChanged(
        eventTime: EventTime,
        playbackSuppressionReason: Int
    ) {
        logd(
            eventTime,
            "playbackSuppressionReason",
            getPlaybackSuppressionReasonString(playbackSuppressionReason)
        )
    }

    override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
        logd(eventTime, "isPlaying", isPlaying.toString())
    }

    override fun onTimelineChanged(eventTime: EventTime, reason: Int) {
        val periodCount = eventTime.timeline.periodCount
        val windowCount = eventTime.timeline.windowCount
        logd(
            "timeline ["
                    + getEventTimeString(eventTime)
                    + ", periodCount="
                    + periodCount
                    + ", windowCount="
                    + windowCount
                    + ", reason="
                    + getTimelineChangeReasonString(reason)
        )
        for (i in 0 until Math.min(periodCount, MAX_TIMELINE_ITEM_LINES)) {
            eventTime.timeline.getPeriod(i, (period)!!)
            logd("  " + "period [" + getTimeString(period!!.durationMs) + "]")
        }
        if (periodCount > MAX_TIMELINE_ITEM_LINES) {
            logd("  ...")
        }
        for (i in 0 until Math.min(windowCount, MAX_TIMELINE_ITEM_LINES)) {
            eventTime.timeline.getWindow(i, (window)!!)
            logd(
                ("  "
                        + "window ["
                        + getTimeString(window!!.durationMs)
                        + ", seekable="
                        + window!!.isSeekable
                        + ", dynamic="
                        + window!!.isDynamic
                        + "]")
            )
        }
        if (windowCount > MAX_TIMELINE_ITEM_LINES) {
            logd("  ...")
        }
        logd("]")
    }

    override fun onMediaItemTransition(eventTime: EventTime, mediaItem: MediaItem?, reason: Int) {
        logd(
            "mediaItem ["
                    + getEventTimeString(eventTime)
                    + ", reason="
                    + getMediaItemTransitionReasonString(reason)
                    + "]"
        )
    }

    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: PositionInfo,
        newPosition: PositionInfo,
        reason: Int
    ) {
        val builder = java.lang.StringBuilder()
        builder
            .append("reason=")
            .append(getDiscontinuityReasonString(reason))
            .append(", PositionInfo:old [")
            .append("mediaItem=")
            .append(oldPosition.mediaItemIndex)
            .append(", period=")
            .append(oldPosition.periodIndex)
            .append(", pos=")
            .append(oldPosition.positionMs)
        if (oldPosition.adGroupIndex != androidx.media3.common.C.INDEX_UNSET) {
            builder
                .append(", contentPos=")
                .append(oldPosition.contentPositionMs)
                .append(", adGroup=")
                .append(oldPosition.adGroupIndex)
                .append(", ad=")
                .append(oldPosition.adIndexInAdGroup)
        }
        builder
            .append("], PositionInfo:new [")
            .append("mediaItem=")
            .append(newPosition.mediaItemIndex)
            .append(", period=")
            .append(newPosition.periodIndex)
            .append(", pos=")
            .append(newPosition.positionMs)
        if (newPosition.adGroupIndex != androidx.media3.common.C.INDEX_UNSET) {
            builder
                .append(", contentPos=")
                .append(newPosition.contentPositionMs)
                .append(", adGroup=")
                .append(newPosition.adGroupIndex)
                .append(", ad=")
                .append(newPosition.adIndexInAdGroup)
        }
        builder.append("]")
        logd(eventTime, "positionDiscontinuity", builder.toString())
    }

    override fun onPlaybackParametersChanged(
        eventTime: EventTime,
        playbackParameters: androidx.media3.common.PlaybackParameters
    ) {
        logd(eventTime, "playbackParameters", playbackParameters.toString())
    }

    override fun onSeekBackIncrementChanged(eventTime: EventTime, seekBackIncrementMs: Long) {
        logd(eventTime, "onSeekBackIncrementChanged", seekBackIncrementMs.toString())
    }

    override fun onSeekForwardIncrementChanged(eventTime: EventTime, seekForwardIncrementMs: Long) {
        logd(eventTime, "onSeekForwardIncrementChanged", seekForwardIncrementMs.toString())
    }

    override fun onMaxSeekToPreviousPositionChanged(
        eventTime: EventTime,
        maxSeekToPreviousPositionMs: Long
    ) {
        logd(eventTime, "onMaxSeekToPreviousPositionChanged", maxSeekToPreviousPositionMs.toString())
    }

    override fun onRepeatModeChanged(eventTime: EventTime, repeatMode: Int) {
        super.onRepeatModeChanged(eventTime, repeatMode)
    }

    override fun onShuffleModeChanged(eventTime: EventTime, shuffleModeEnabled: Boolean) {
        logd(eventTime, "shuffleModeEnabled", shuffleModeEnabled.toString())
    }

    override fun onIsLoadingChanged(eventTime: EventTime, isLoading: Boolean) {
        logd(eventTime, "loading", java.lang.Boolean.toString(isLoading))
    }

    override fun onAvailableCommandsChanged(eventTime: EventTime, availableCommands: Commands) {
        logd(eventTime, "onAvailableCommandsChanged", availableCommands.toString())
    }

    override fun onPlayerError(
        eventTime: EventTime,
        error: androidx.media3.common.PlaybackException
    ) {
        loge(eventTime, "playerFailed", error)
    }

    override fun onPlayerErrorChanged(
        eventTime: EventTime,
        error: androidx.media3.common.PlaybackException?
    ) {
        loge(eventTime, "playerFailed", error)
    }

    override fun onTracksChanged(eventTime: EventTime, tracks: androidx.media3.common.Tracks) {
        logd("tracks [" + getEventTimeString(eventTime))
        // Log tracks associated to renderers.
        // Log tracks associated to renderers.
        val trackGroups: ImmutableList<TracksGroup> = tracks.groups
        for (groupIndex in trackGroups.indices) {
            val trackGroup: TracksGroup = trackGroups[groupIndex]
            logd("  group [")
            for (trackIndex in 0 until trackGroup.length) {
                val status = getTrackStatusString(trackGroup.isTrackSelected(trackIndex))
                val formatSupport =
                    Util.getFormatSupportString(trackGroup.getTrackSupport(trackIndex))
                logd(
                    "    "
                            + status
                            + " Track:"
                            + trackIndex
                            + ", "
                            + Format.toLogString(trackGroup.getTrackFormat(trackIndex))
                            + ", supported="
                            + formatSupport
                )
            }
            logd("  ]")
        }
        // TODO: Replace this with an override of onMediaMetadataChanged.
        // Log metadata for at most one of the selected tracks.
        // TODO: Replace this with an override of onMediaMetadataChanged.
        // Log metadata for at most one of the selected tracks.
        var loggedMetadata = false
        run {
            var groupIndex = 0
            while (!loggedMetadata && groupIndex < trackGroups.size) {
                val trackGroup: TracksGroup = trackGroups[groupIndex]
                var trackIndex = 0
                while (!loggedMetadata && trackIndex < trackGroup.length) {
                    if (trackGroup.isTrackSelected(trackIndex)) {
                        val metadata: Metadata? = trackGroup.getTrackFormat(trackIndex).metadata
                        if (metadata != null && metadata.length() > 0) {
                            logd("  Metadata [")
                            printMetadata(metadata, "    ")
                            logd("  ]")
                            loggedMetadata = true
                        }
                    }
                    trackIndex++
                }
                groupIndex++
            }
        }
        logd("]")
    }

    override fun onTrackSelectionParametersChanged(
        eventTime: EventTime,
        trackSelectionParameters: TrackSelectionParameters
    ) {
        super.onTrackSelectionParametersChanged(eventTime, trackSelectionParameters)
    }

    override fun onMediaMetadataChanged(eventTime: EventTime, mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(eventTime, mediaMetadata)
    }

    override fun onPlaylistMetadataChanged(eventTime: EventTime, playlistMetadata: MediaMetadata) {
        super.onPlaylistMetadataChanged(eventTime, playlistMetadata)
    }

    override fun onLoadStarted(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadCompleted(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadCanceled(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        super.onLoadCanceled(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadError(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        printInternalError(eventTime, "loadError", error)
    }

    override fun onDownstreamFormatChanged(eventTime: EventTime, mediaLoadData: MediaLoadData) {
        logd(eventTime, "downstreamFormat", Format.toLogString(mediaLoadData.trackFormat))
    }

    override fun onUpstreamDiscarded(eventTime: EventTime, mediaLoadData: MediaLoadData) {
        logd(eventTime, "upstreamDiscarded", Format.toLogString(mediaLoadData.trackFormat))
    }

    override fun onBandwidthEstimate(
        eventTime: EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) {
        super.onBandwidthEstimate(eventTime, totalLoadTimeMs, totalBytesLoaded, bitrateEstimate)
    }

    override fun onMetadata(eventTime: EventTime, metadata: Metadata) {
        logd("metadata [" + getEventTimeString(eventTime))
        printMetadata(metadata, "  ")
        logd("]")
    }

    override fun onCues(eventTime: EventTime, cueGroup: CueGroup) {
        super.onCues(eventTime, cueGroup)
    }

    override fun onAudioEnabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "audioEnabled")
    }

    override fun onAudioDecoderInitialized(
        eventTime: EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        logd(eventTime, "audioDecoderInitialized", decoderName)
    }

    override fun onAudioInputFormatChanged(
        eventTime: EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        logd(eventTime, "audioInputFormat", Format.toLogString(format))
    }

    override fun onAudioPositionAdvancing(eventTime: EventTime, playoutStartSystemTimeMs: Long) {
        super.onAudioPositionAdvancing(eventTime, playoutStartSystemTimeMs)
    }

    override fun onAudioUnderrun(
        eventTime: EventTime,
        bufferSize: Int,
        bufferSizeMs: Long,
        elapsedSinceLastFeedMs: Long
    ) {
        loge(
            eventTime,
            "audioTrackUnderrun",
            "$bufferSize, $bufferSizeMs, $elapsedSinceLastFeedMs",  /* throwable= */
            null
        )
    }

    override fun onAudioDecoderReleased(eventTime: EventTime, decoderName: String) {
        logd(eventTime, "audioDecoderReleased", decoderName)
    }

    override fun onAudioDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "audioDisabled")
    }

    override fun onAudioSessionIdChanged(eventTime: EventTime, audioSessionId: Int) {
        logd(eventTime, "audioSessionId", Integer.toString(audioSessionId))
    }

    override fun onAudioAttributesChanged(eventTime: EventTime, audioAttributes: AudioAttributes) {
        logd(
            eventTime,
            "audioAttributes",
            audioAttributes.contentType
                .toString() + ","
                    + audioAttributes.flags
                    + ","
                    + audioAttributes.usage
                    + ","
                    + audioAttributes.allowedCapturePolicy
        )
    }

    override fun onSkipSilenceEnabledChanged(eventTime: EventTime, skipSilenceEnabled: Boolean) {
        logd(eventTime, "skipSilenceEnabled", java.lang.Boolean.toString(skipSilenceEnabled))
    }

    override fun onAudioSinkError(eventTime: EventTime, audioSinkError: java.lang.Exception) {
        printInternalError(eventTime, "onAudioSinkError", audioSinkError)
    }

    override fun onAudioCodecError(eventTime: EventTime, audioCodecError: java.lang.Exception) {
        printInternalError(eventTime, "onAudioCodecError", audioCodecError)
    }

    override fun onVolumeChanged(eventTime: EventTime, volume: Float) {
        logd(eventTime, "volume", java.lang.Float.toString(volume))
    }

    override fun onDeviceInfoChanged(
        eventTime: EventTime,
        deviceInfo: androidx.media3.common.DeviceInfo
    ) {
        super.onDeviceInfoChanged(eventTime, deviceInfo)
    }

    override fun onDeviceVolumeChanged(eventTime: EventTime, volume: Int, muted: Boolean) {
        super.onDeviceVolumeChanged(eventTime, volume, muted)
    }

    override fun onVideoEnabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "videoEnabled")
    }

    override fun onVideoDecoderInitialized(
        eventTime: EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        logd(eventTime, "videoDecoderInitialized", decoderName)
    }

    override fun onVideoInputFormatChanged(
        eventTime: EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        logd(eventTime, "videoInputFormat", Format.toLogString(format))
    }

    override fun onDroppedVideoFrames(eventTime: EventTime, droppedFrames: Int, elapsedMs: Long) {
        logd(eventTime, "droppedFrames", Integer.toString(droppedFrames))
    }

    override fun onVideoDecoderReleased(eventTime: EventTime, decoderName: String) {
        logd(eventTime, "videoDecoderReleased", decoderName)
    }

    override fun onVideoDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "videoDisabled")
    }

    override fun onVideoFrameProcessingOffset(
        eventTime: EventTime,
        totalProcessingOffsetUs: Long,
        frameCount: Int
    ) {
        super.onVideoFrameProcessingOffset(eventTime, totalProcessingOffsetUs, frameCount)
    }

    override fun onVideoCodecError(eventTime: EventTime, videoCodecError: java.lang.Exception) {
        printInternalError(eventTime, "onVideoCodecError", videoCodecError)
    }

    override fun onRenderedFirstFrame(eventTime: EventTime, output: Any, renderTimeMs: Long) {
        logd(eventTime, "renderedFirstFrame", output.toString())
    }

    override fun onVideoSizeChanged(
        eventTime: EventTime,
        videoSize: androidx.media3.common.VideoSize
    ) {
        logd(eventTime, "videoSize", videoSize.width.toString() + ", " + videoSize.height)
    }

    override fun onSurfaceSizeChanged(eventTime: EventTime, width: Int, height: Int) {
        logd(eventTime, "surfaceSize", "$width, $height")
    }

    override fun onDrmSessionAcquired(eventTime: EventTime, state: Int) {
        logd(eventTime, "drmSessionAcquired", "state=$state")
    }

    override fun onDrmKeysLoaded(eventTime: EventTime) {
        logd(eventTime, "drmKeysLoaded")
    }

    override fun onDrmSessionManagerError(eventTime: EventTime, error: java.lang.Exception) {
        printInternalError(eventTime, "drmSessionManagerError", error)
    }

    override fun onDrmKeysRestored(eventTime: EventTime) {
        logd(eventTime, "drmKeysRestored")
    }

    override fun onDrmKeysRemoved(eventTime: EventTime) {
        logd(eventTime, "drmKeysRemoved")
    }

    override fun onDrmSessionReleased(eventTime: EventTime) {
        logd(eventTime, "drmSessionReleased")
    }

    override fun onPlayerReleased(eventTime: EventTime) {
        logd(eventTime, "onPlayerReleased")
    }

    override fun onEvents(player: androidx.media3.common.Player, events: AnalyticsListener.Events) {
        super.onEvents(player, events)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }

    private fun logd(msg: String) {
        Log.d(tag, msg)
        val timestampedMsg = "${getCurrentTimestamp()} - $msg\n"
        _eventLogString = timestampedMsg
        logTextCallback(eventLogString)
    }

    private fun loge(msg: String) {
        Log.e(tag, msg)
        val timestampedMsg = "${getCurrentTimestamp()} - $msg\n"
        _eventLogString = timestampedMsg
        logTextCallback(eventLogString)
    }

    // Internal methods
    private fun logd(eventTime: EventTime, eventName: String) {
        logd(
            getEventString(
                eventTime,
                eventName,  /* eventDescription= */
                null,  /* throwable= */
                null
            )
        )
    }

    private fun logd(eventTime: EventTime, eventName: String, eventDescription: String) {
        logd(getEventString(eventTime, eventName, eventDescription,  /* throwable= */null))
    }

    private fun loge(eventTime: EventTime, eventName: String, throwable: Throwable?) {
        loge(getEventString(eventTime, eventName,  /* eventDescription= */null, throwable))
    }

    private fun loge(
        eventTime: EventTime,
        eventName: String,
        eventDescription: String,
        throwable: Throwable?
    ) {
        loge(getEventString(eventTime, eventName, eventDescription, throwable))
    }

    private fun printInternalError(eventTime: EventTime, type: String, e: java.lang.Exception) {
        loge(eventTime, "internalError", type, e)
    }

    private fun printMetadata(metadata: Metadata, prefix: String) {
        for (i in 0 until metadata.length()) {
            logd(prefix + metadata[i])
        }
    }

    private fun getEventString(
        eventTime: EventTime, eventName: String, eventDescription: String?, throwable: Throwable?
    ): String {
        var eventString = eventName + " [" + getEventTimeString(eventTime)
        if (throwable is androidx.media3.common.PlaybackException) {
            eventString += ", errorCode=" + throwable.errorCodeName
        }
        if (eventDescription != null) {
            eventString += ", $eventDescription"
        }
        val throwableString = Log.getThrowableString(throwable)
        if (!TextUtils.isEmpty(throwableString)) {
            eventString += """
  ${throwableString!!.replace("\n", "\n  ")}
"""
        }
        eventString += "]"
        return eventString
    }

    private fun getEventTimeString(eventTime: EventTime): String? {
        var windowPeriodString = "window=" + eventTime.windowIndex
        if (eventTime.mediaPeriodId != null) {
            windowPeriodString += ", period=" + eventTime.timeline.getIndexOfPeriod(eventTime.mediaPeriodId!!.periodUid)
            if (eventTime.mediaPeriodId!!.isAd) {
                windowPeriodString += ", adGroup=" + eventTime.mediaPeriodId!!.adGroupIndex
                windowPeriodString += ", ad=" + eventTime.mediaPeriodId!!.adIndexInAdGroup
            }
        }
        return ("eventTime="
                + getTimeString(eventTime.realtimeMs - startTimeMs)
                + ", mediaPos="
                + getTimeString(eventTime.eventPlaybackPositionMs)
                + ", "
                + windowPeriodString)
    }

    private fun getTimeString(timeMs: Long): String {
        return if (timeMs == C.TIME_UNSET) "?" else TIME_FORMAT.format((timeMs / 1000f).toDouble())
    }

    private fun getStateString(state: Int): String {
        return when (state) {
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_ENDED -> "ENDED"
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_READY -> "READY"
            else -> "?"
        }
    }

    private fun getTrackStatusString(selected: Boolean): String {
        return if (selected) "[X]" else "[ ]"
    }

//  private fun getRepeatModeString(repeatMode: @Player.RepeatMode Int): String {
//    return when (repeatMode) {
//      Player.REPEAT_MODE_OFF -> "OFF"
//      Player.REPEAT_MODE_ONE -> "ONE"
//      Player.REPEAT_MODE_ALL -> "ALL"
//      else -> "?"
//    }
//  }

    private fun getDiscontinuityReasonString(reason: @DiscontinuityReason Int): String {
        return when (reason) {
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> "AUTO_TRANSITION"
            Player.DISCONTINUITY_REASON_SEEK -> "SEEK"
            Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> "SEEK_ADJUSTMENT"
            Player.DISCONTINUITY_REASON_REMOVE -> "REMOVE"
            Player.DISCONTINUITY_REASON_SKIP -> "SKIP"
            Player.DISCONTINUITY_REASON_INTERNAL -> "INTERNAL"
            else -> "?"
        }
    }

    private fun getTimelineChangeReasonString(reason: @TimelineChangeReason Int): String {
        return when (reason) {
            Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> "SOURCE_UPDATE"
            Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> "PLAYLIST_CHANGED"
            else -> "?"
        }
    }

    private fun getMediaItemTransitionReasonString(
        reason: @MediaItemTransitionReason Int
    ): String {
        return when (reason) {
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> "AUTO"
            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> "PLAYLIST_CHANGED"
            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> "REPEAT"
            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> "SEEK"
            else -> "?"
        }
    }

    private fun getPlaybackSuppressionReasonString(
        playbackSuppressionReason: @PlaybackSuppressionReason Int
    ): String {
        return when (playbackSuppressionReason) {
            Player.PLAYBACK_SUPPRESSION_REASON_NONE -> "NONE"
            Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS -> "TRANSIENT_AUDIO_FOCUS_LOSS"
            else -> "?"
        }
    }

    private fun getPlayWhenReadyChangeReasonString(
        reason: @PlayWhenReadyChangeReason Int
    ): String {
        return when (reason) {
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY -> "AUDIO_BECOMING_NOISY"
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS -> "AUDIO_FOCUS_LOSS"
            Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE -> "REMOTE"
            Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST -> "USER_REQUEST"
            Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM -> "END_OF_MEDIA_ITEM"
            else -> "?"
        }
    }
}