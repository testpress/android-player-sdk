package com.tpstream.player.ui

import android.media.MediaDrm
import android.os.Build
import androidx.core.view.isVisible
import com.tpstream.player.C
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
        } catch (e: Exception) {
            debugOverlayLayout.drmDetailContainer.isVisible = false
        }
    }

    fun updateVideoDetails() {
        showVideoDetails()
    }

    private fun showVideoDetails() {
        debugOverlayLayout.videoDetailContainer.isVisible = true

        val videoFormat = player.exoPlayer.videoFormat
        val audioFormat = player.exoPlayer.audioFormat

        val videoFormatString = buildString {
            append("Video Format:")
            append("Track:${videoFormat?.id ?: "N/A"}/${videoFormat?.width}x${videoFormat?.height}@${videoFormat?.frameRate}/")
            append("Bitrate:${videoFormat?.bitrate ?: "N/A"}/")
            append("Supported:${getSupportedValue(true) ?: "Unknown"}")
        }

        val audioFormatString = buildString {
            append("Audio Format:")
            append("Track:${audioFormat?.id ?: "N/A"}/")
            append("Bitrate:${audioFormat?.bitrate ?: "N/A"}/")
            append("Supported:${getSupportedValue(false) ?: "Unknown"}")
        }

        debugOverlayLayout.videoFormat.text = videoFormatString
        debugOverlayLayout.audioFormat.text = audioFormatString
    }

    private fun getSupportedValue(isVideo: Boolean): String? {
        val trackType = if (isVideo) C.TRACK_TYPE_VIDEO else C.TRACK_TYPE_AUDIO

        player.exoPlayer.currentTracks.groups.firstOrNull { it.type == trackType }
            ?.let { group ->
                for (trackIndex in 0 until group.length) {
                    if (group.isTrackSelected(trackIndex)) {
                        return Util.getFormatSupportString(group.getTrackSupport(trackIndex))
                    }
                }
            }
        return null
    }


}