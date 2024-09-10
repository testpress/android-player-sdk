package com.tpstream.app

import android.content.Intent
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import com.tpstream.player.offline.TpStreamDownloadManager

class MainActivity : AppCompatActivity() {

    val TAG = "TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: ${getMaxSupportedPlaybackResolution()}")
    }

    fun getMaxSupportedPlaybackResolution(): Pair<Int, Int> {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        var maxWidth = 0
        var maxHeight = 0

        // Iterate through all codec infos
        codecList.codecInfos.forEach { codecInfo ->
            if (!codecInfo.isEncoder) { // We are interested in decoders only

                codecInfo.supportedTypes.forEach { mimeType ->
                    if (mimeType.equals("video/avc", ignoreCase = true)) {
                        Log.d(TAG, "codecInfo.name: ${codecInfo.name}")
                        Log.d(TAG, "isHardwareAccelerated: ${codecInfo.isHardwareAccelerated}")
                        // Get codec capabilities
                        val codecCapabilities = codecInfo.getCapabilitiesForType(mimeType)
                        codecCapabilities.videoCapabilities?.let { videoCapabilities ->
                            // Get maximum supported width and height
                            val supportedWidth = videoCapabilities.supportedWidths
                            val supportedHeight = videoCapabilities.supportedHeights

                            logSupportedVideoCapabilities(videoCapabilities)

                            // Update max width and height
                            if (supportedWidth.upper > maxWidth) maxWidth = supportedWidth.upper
                            if (supportedHeight.upper > maxHeight) maxHeight = supportedHeight.upper
                        }
                    }
                }
            }
        }

        return Pair(maxWidth, maxHeight)
    }

    fun logSupportedVideoCapabilities(videoCapabilities: MediaCodecInfo.VideoCapabilities) {
        // List of resolutions to check, using Triple<Width, Height, FrameRate>
        val resolutions = listOf(
            Triple(426, 240, 36.0),   // 240p
            Triple(640, 360, 36.0),   // 360p
            Triple(854, 480, 36.0),   // 480p
            Triple(1280, 720, 36.0),  // 720p
            Triple(1920, 1080, 36.0), // 1080p
            Triple(3840, 2160, 36.0)  // 4K
        )

        // Log supported widths and heights
        Log.d(TAG, "Supported Widths: ${videoCapabilities.supportedWidths}")
        Log.d(TAG, "Supported Heights: ${videoCapabilities.supportedHeights}")
        Log.d(TAG, "Supported Frame Rates: ${videoCapabilities.supportedFrameRates}")

        // Loop through each resolution and log the support details
        for ((width, height, frameRate) in resolutions) {
            // Check if the resolution size is supported
            val isSizeSupported = videoCapabilities.isSizeSupported(width, height)
            Log.d(TAG, "Supported frames for ${width}x$height: $isSizeSupported")

            // If the size is supported, log additional details
            if (isSizeSupported) {
                // Check if the size and frame rate are supported
                val isRateSupported = videoCapabilities.areSizeAndRateSupported(width, height, frameRate)
                Log.d(TAG, "Are size and rate supported for ${width}x$height at ${frameRate}fps: $isRateSupported")

                // Get and log supported frame rates for the resolution
                val supportedRates = videoCapabilities.getSupportedFrameRatesFor(width, height)
                Log.d(TAG, "Supported frame rates for ${width}x$height: $supportedRates")
            }
        }
    }


    fun buttonClick(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TP_DRM")
        startActivity(myIntent)
    }

    fun buttonClick2(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TP_AES_Encrypt")
        startActivity(myIntent)
    }

    fun buttonClick3(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TP_NON_DRM")
        startActivity(myIntent)
    }

    fun buttonClick4(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TPS_DRM")
        startActivity(myIntent)
    }

    fun buttonClick5(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TPS_NON_DRM")
        startActivity(myIntent)
    }

    fun downloadButton(view: View) {
        val myIntent = Intent(this, DownloadListActivity::class.java)
        startActivity(myIntent)
    }

    fun downloadDRMVideo(view: View) {
        TPStreamsSDK.initialize(TPStreamsSDK.Provider.TPStreams, "6eafqn")
        val parameters = TpInitParams.Builder()
            .setVideoId("6suEBPy7EG4")
            .setAccessToken("ab70caed-6168-497f-89c1-1e308da2c9aa")
            .build()
        TpStreamDownloadManager(applicationContext).startDownload(this,parameters)
    }

}