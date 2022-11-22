package com.tpstream.player.views

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.collect.ImmutableList
import com.tpstream.player.DownloadTask
import com.tpstream.player.TpInitParams
import com.tpstream.player.VideoDownloadRequestCreationHandler
import com.tpstream.player.databinding.DownloadTrackSelectionDialogBinding
import com.tpstream.player.models.VideoInfo
import okio.IOException
import com.tpstream.player.R

class DownloadResolutionSelectionSheet(
    parameters: DefaultTrackSelector.Parameters,
    private val trackGroups: List<Tracks.Group>,
    private val videoInfo: VideoInfo,
    private val tpInitParams: TpInitParams
) : BottomSheetDialogFragment(), VideoDownloadRequestCreationHandler.Listener {

    private var _binding: DownloadTrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    var overrides: MutableMap<TrackGroup, TrackSelectionOverride> =
        parameters.overrides.toMutableMap()

    private lateinit var videoDownloadRequestCreateHandler: VideoDownloadRequestCreationHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoDownloadRequestCreateHandler =
            VideoDownloadRequestCreationHandler(
                requireContext(),
                videoInfo,
                tpInitParams
            )
        videoDownloadRequestCreateHandler.listener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DownloadTrackSelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTrackSelectionView()
        setOnClickListeners()
        configureBottomSheetBehaviour()
    }

    private fun initializeTrackSelectionView() {
        val trackInfos = getTrackInfos()
        val adapter = Adapter(requireContext(), trackInfos, null)
        binding.listview.also {
            it.adapter = adapter
            it.setOnItemClickListener { _, _, index, _ ->
                adapter.trackPosition = index
                adapter.notifyDataSetChanged()
                val resolution = trackInfos[index]
                val mediaTrackGroup: TrackGroup = resolution.trackGroup.mediaTrackGroup
                overrides.clear()
                overrides[mediaTrackGroup] =
                    TrackSelectionOverride(mediaTrackGroup, ImmutableList.of(resolution.trackIndex))
            }
        }
    }

    private fun setOnClickListeners() {
        binding.startDownload.setOnClickListener {
            val downloadRequest =
                videoDownloadRequestCreateHandler.buildDownloadRequest(overrides)
            DownloadTask(
                downloadRequest.uri.toString(),
                requireContext()
            ).start(downloadRequest)
            Toast.makeText(requireContext(), "Download Start", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.cancelDownload.setOnClickListener { dismiss() }

    }

    private fun getTrackInfos(): ArrayList<TrackInfo> {
        val trackInfos = arrayListOf<TrackInfo>()
        if (trackGroups.none { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }) {
            return trackInfos
        }

        val trackGroup = trackGroups.first { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }
        for (trackIndex in 0 until trackGroup.length) {
            trackInfos.add(TrackInfo(trackGroup, trackIndex))
        }
        return trackInfos
    }

    private fun configureBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.behavior.isDraggable = true
        bottomSheetDialog.behavior.isFitToContents = true
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    inner class Adapter(
        context1: Context,
        dataSource: ArrayList<TrackInfo>,
        var trackPosition: Int?
    ) : ArrayAdapter<TrackInfo>(context1, R.layout.download_resulotion_data, dataSource) {
        private val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val resolution = getItem(position)!!
            var view = convertView
            if (convertView == null) {
                view = inflater.inflate(R.layout.download_resulotion_data, parent, false)
            }
            val track = view!!.findViewById<CheckedTextView>(R.id.track_selecting)
            track.text = "${resolution.format.height}p"

            track.isChecked = trackPosition == position

            return view
        }
    }

    inner class TrackInfo(val trackGroup: Tracks.Group, val trackIndex: Int) {
        val format: Format
            get() = trackGroup.getTrackFormat(trackIndex)
    }

    override fun onDownloadRequestHandlerPrepared(isPrepared: Boolean) {
        if (isPrepared) {
            binding.loadingProgress.visibility = View.GONE
            binding.resolutionLayout.visibility = View.VISIBLE
        }
    }

    override fun onDownloadRequestHandlerPrepareError(helper: DownloadHelper, e: IOException) {
        binding.startDownload.setOnClickListener {
            dismiss()
        }
        Log.d("VideoDownload", "onDownloadRequestHandlerPrepareError: ${e.localizedMessage}")
    }

}