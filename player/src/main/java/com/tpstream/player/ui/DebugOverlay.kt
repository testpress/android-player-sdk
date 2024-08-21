package com.tpstream.player.ui

import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import android.util.Log
import androidx.core.view.isVisible
import com.tpstream.player.C
import com.tpstream.player.Format
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpStreamPlayerImpl
import com.tpstream.player.Util
import com.tpstream.player.databinding.DebugOverlayLayoutBinding

internal class DebugOverlay(
    private val player: TpStreamPlayerImpl,
    private val debugOverlayLayout: DebugOverlayLayoutBinding
) {

    init {
        showAssetDetails()
        if (player.asset?.video?.isDrmProtected == true) {
            showDRMDetails()
        }
        showVideoDetails()
    }
    private fun showAssetDetails() {
        debugOverlayLayout.root.isVisible = true
        debugOverlayLayout.closeButton.setOnClickListener {
            debugOverlayLayout.root.isVisible = false
        }
        try {
            debugOverlayLayout.assetDetailContainer.isVisible = true
            debugOverlayLayout.assetDetailContainer.isClickable = false
            debugOverlayLayout.providerAndOrgCodeAndVideoId.text =
                "${TPStreamsSDK.provider.name}/${TPStreamsSDK.orgCode}/${player.asset?.id}"
        } catch (e: Exception) {
            Log.d("TAG", "showAssetDetails: ")
            debugOverlayLayout.assetDetailContainer.isVisible = false
        }
    }

    private fun showDRMDetails() {
        try {
            debugOverlayLayout.drmDetailContainer.isVisible = true
            val mediaDrm = MediaDrm(C.WIDEVINE_UUID)
            val drmInfoString = buildString {
                append("${mediaDrm.getPropertyString("securityLevel")}/")
                append("${mediaDrm.getPropertyString(MediaDrm.PROPERTY_VERSION)}/")
                append("${mediaDrm.getPropertyString("systemId")}")
            }
            debugOverlayLayout.levelAndVersionAndSystemId.text = drmInfoString
            if (Build.VERSION.SDK_INT >= 28) {
                mediaDrm.close()
            } else {
                mediaDrm.release()
            }
        } catch (e: UnsupportedSchemeException) {
            debugOverlayLayout.levelAndVersionAndSystemId.text = "DRM not supported on this device"
        } catch (e: SecurityException) {
            debugOverlayLayout.levelAndVersionAndSystemId.text = "Security error accessing DRM"
        } catch (e: Exception) {
            debugOverlayLayout.levelAndVersionAndSystemId.text = "An unexpected error occurred while retrieving DRM details"
        }
    }

    private fun showVideoDetails() {
        debugOverlayLayout.videoDetailContainer.isVisible = true
        var isDefaultVideoIndex = false
        var isDefaultAudioIndex = false
        var videoFormat = player.exoPlayer.videoFormat
        var audioFormat = player.exoPlayer.audioFormat

        if (videoFormat == null){
            isDefaultVideoIndex = true
            videoFormat = getFirstFormat(true)
        }
        if (audioFormat == null){
            isDefaultAudioIndex = true
            audioFormat = getFirstFormat(false)
        }

        val videoFormatString = buildString {
            append("Video Format:")
            append(getTrackStatusString(videoFormat.isSelected(isDefaultVideoIndex,true)))
            append("Track:${videoFormat?.id ?: "N/A"}/${videoFormat?.width}x${videoFormat?.height}@${videoFormat?.frameRate}/")
            append("Bitrate:${videoFormat?.bitrate ?: "N/A"}/")
            append("Supported:${getSupportedValue(isDefaultVideoIndex, true) ?: "Unknown"}")
        }

        val audioFormatString = buildString {
            append("Audio Format:")
            append(getTrackStatusString(audioFormat.isSelected(isDefaultAudioIndex,false)))
            append("Track:${audioFormat?.id ?: "N/A"}/")
            append("Bitrate:${audioFormat?.bitrate ?: "N/A"}/")
            append("Supported:${getSupportedValue(isDefaultAudioIndex, false) ?: "Unknown"}")
        }

        debugOverlayLayout.videoFormat.text = videoFormatString
        debugOverlayLayout.audioFormat.text = audioFormatString
    }

    private fun getFirstFormat(isVideo: Boolean): Format? {
        val trackType = if (isVideo) C.TRACK_TYPE_VIDEO else C.TRACK_TYPE_AUDIO
        player.exoPlayer.currentTracks.groups.firstOrNull { it.type == trackType }
            ?.let { group ->
                return group.getTrackFormat(0)
            }
        return null
    }

    private fun getTrackStatusString(selected: Boolean): String? {
        return if (selected) "[X]" else "[ ]"
    }

    private fun Format?.isSelected(isDefaultIndex: Boolean, isVideo: Boolean): Boolean {
        if (this == null) return false
        val trackType = if (isVideo) C.TRACK_TYPE_VIDEO else C.TRACK_TYPE_AUDIO

        player.exoPlayer.currentTracks.groups.firstOrNull { it.type == trackType }
            ?.let { group ->
                if (isDefaultIndex) {
                    return group.isTrackSelected(0)
                } else {
                    for (trackIndex in 0 until group.length) {
                        if (group.getTrackFormat(trackIndex).id == this.id) {
                            return group.isTrackSelected(trackIndex)
                        }
                    }
                }
            }
        return false
    }

    private fun getSupportedValue(isDefaultIndex: Boolean, isVideo: Boolean): String? {
        val trackType = if (isVideo) C.TRACK_TYPE_VIDEO else C.TRACK_TYPE_AUDIO

        player.exoPlayer.currentTracks.groups.firstOrNull { it.type == trackType }
            ?.let { group ->
                if (isDefaultIndex) {
                    return Util.getFormatSupportString(group.getTrackSupport(0))
                } else {
                    for (trackIndex in 0 until group.length) {
                        if (group.isTrackSelected(trackIndex)) {
                            return Util.getFormatSupportString(group.getTrackSupport(trackIndex))
                        }
                    }
                }
            }
        return null
    }

    fun updateVideoDetails() {
        showVideoDetails()
    }
}