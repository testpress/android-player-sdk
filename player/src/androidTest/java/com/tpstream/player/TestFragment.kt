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
        view.findViewById<Button>(R.id.bottom_sheet_button).setOnClickListener {
            val downloadResolutionSelectionSheet = DownloadResolutionSelectionSheet(
                player,
                DefaultTrackSelector.Parameters.Builder(requireContext()).build(),
                immutableListOf(
                    Tracks.Group(
                        TrackGroup(
                            Format.Builder()
                                .setHeight(1080)
                                .setAverageBitrate(1000000)
                                .build()
                        ),
                        false,
                        intArrayOf(C.FORMAT_HANDLED),
                        booleanArrayOf(true)
                    )
                )
            )
            downloadResolutionSelectionSheet.show(
                requireActivity().supportFragmentManager,
                "AdvancedSheetDownload"
            )
        }
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