package com.tpstream.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.models.VideoInfo
import com.tpstream.player.views.DownloadResolutionSelectionSheet
import okhttp3.internal.immutableListOf
import org.mockito.Mock

class TestFragment : Fragment() {

    @Mock
    lateinit var player: TpStreamPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.frame_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTpPlayer()

        val format1: Format = videoFormat(500, 0, 1080)
        val format2: Format = videoFormat(1940185,0,720)
        val format3: Format = videoFormat(837776, 0, 360)
        val format4: Format = videoFormat(413306, 0, 240)

        view.findViewById<Button>(R.id.bottom_sheet_button).setOnClickListener {
            val downloadResolutionSelectionSheet = DownloadResolutionSelectionSheet(
                player,
                DefaultTrackSelector.Parameters.Builder(requireContext()).build(),
                immutableListOf(
                    Tracks.Group(
                        TrackGroup(format1,format2,format3,format4),
                        true,
                        intArrayOf(C.FORMAT_HANDLED,C.FORMAT_HANDLED,C.FORMAT_HANDLED,C.FORMAT_HANDLED),
                        booleanArrayOf(true,false,false,false)
                    )
                )
            )
            downloadResolutionSelectionSheet.show(
                requireActivity().supportFragmentManager,
                "AdvancedSheetDownload"
            )
        }
    }

    private fun videoFormat(bitrate: Int, width: Int, height: Int): Format {
        return Format.Builder()
            .setSampleMimeType(MimeTypes.VIDEO_H264)
            .setAverageBitrate(bitrate)
            .setPeakBitrate(bitrate)
            .setWidth(width)
            .setHeight(height)
            .build()
    }

    private fun initTpPlayer() {
        val tpStreamPlayerImpl =
            TpStreamPlayerImpl(
                ExoPlayer.Builder(requireContext()).build(),
                requireContext()
            )

        tpStreamPlayerImpl.videoInfo = VideoInfo(
            "videoId",
            null,
            null,
            null,
            null,
            "null",
            "null",
            "null",
            null,
            null,
            null
        )

        tpStreamPlayerImpl.params = TpInitParams.Builder()
            .setAccessToken("accessToken")
            .setOrgCode("orgCode")
            .setVideoId("videoId")
            .build()
        player = tpStreamPlayerImpl
    }
}