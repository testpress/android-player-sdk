package com.tpstream.player.data

import android.content.Context
import android.graphics.Bitmap
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.data.source.local.DownloadStatus
import java.text.SimpleDateFormat
import java.util.*

data class Asset(
    var id: String = "",
    var title: String = "",
    var type: String = "",
    var thumbnail: String = "",
    var description: String = "",
    var video: Video = Video(),
    var liveStream: LiveStream? = null,
    val folderTree: String? = null,
    var downloadStartTimeMs: Long = 0,
    var metadata: Map<String, String>? = null
) {
    fun getLocalThumbnail(context: Context): Bitmap?{
        return ImageSaver(context).load(id)
    }

    fun getPlaybackURL(): String? {
        return when {
            this.isLiveStream && this.liveStream?.isStreaming == true -> this.liveStream!!.url
            this.video.isTranscodingCompleted -> this.video.url
            else -> null
        }
    }

    val isLiveStream: Boolean
        get() = type == "livestream"

    fun shouldShowNoticeScreen(): Boolean {
        if (!isLiveStream) return false

        val livestream = this.liveStream!!

        if (livestream.isDisconnected) return true
        if (livestream.isRecording) return true

        return !livestream.isStreaming &&
                !(livestream.isEnded && livestream.recordingEnabled && video.isTranscodingCompleted)
    }

    fun getNoticeMessage(): String? {
        val liveStream = this.liveStream
        val currentDateTime = Date()

        return when {
            liveStream?.noticeMessage != null -> liveStream.noticeMessage
            liveStream?.isNotStarted == true && liveStream.startTime?.after(currentDateTime) == true ->
                "Live stream is scheduled to start at ${liveStream.getFormattedStartTime()}"
            liveStream?.isNotStarted == true ->
                "Live stream will begin soon."
            liveStream?.isDisconnected == true -> "The live stream has been disconnected. Please try again later."
            liveStream?.isRecording == true -> "The live stream has come to an end. Stay tuned, we'll have the recording ready for you shortly."
            liveStream?.isEnded == true && liveStream.recordingEnabled && !video.isTranscodingCompleted ->
                "Live stream has ended. Recording will be available soon."
            liveStream?.isEnded == true && !liveStream.recordingEnabled ->
                "Live stream has concluded. Stay tuned for future broadcasts."
            else -> null
        }
    }
}

data class Video(
    internal var url: String = "",
    var width: Int = 0,
    var height: Int = 0,
    var transcodingStatus: String = "",
    var duration: Long = 0,
    var percentageDownloaded: Int = 0,
    var bytesDownloaded: Long = 0,
    var totalSize: Long = 0,
    var downloadState: DownloadStatus? = null,
    val tracks: List<Track>? = null
){
    val isTranscodingCompleted: Boolean
        get() = transcodingStatus == "Completed"
}

data class LiveStream(
    var url: String,
    var status: String,
    var startTime: Date?,
    var recordingEnabled: Boolean,
    var enabledDRMForRecording: Boolean,
    val enabledDRMForLive: Boolean,
    val noticeMessage: String?
) {
    val isStreaming: Boolean
        get() = status == "Streaming"

    val isEnded: Boolean
        get() = status == "Completed"

    val isNotEnded: Boolean
        get() = !isEnded

    val isDisconnected: Boolean
        get() = status == "Disconnected"

    val isNotStarted: Boolean
        get() = status == "Not Started"

    val isRecording: Boolean
        get() = status == "Recording"

    fun getFormattedStartTime(): String {
        val outputFormat = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault())

        val timeZone = TimeZone.getDefault()

        val formattedStartTime = startTime?.let {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = timeZone
            outputFormat.timeZone = timeZone
            outputFormat.format(it)
        } ?: "N/A"

        return formattedStartTime
    }
}

data class Track(
    val type: String,
    val name: String,
    val url: String,
    val language: String,
    val duration: Long
)