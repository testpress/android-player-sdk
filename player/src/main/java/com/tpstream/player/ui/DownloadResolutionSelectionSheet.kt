package com.tpstream.player.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.collect.ImmutableList
import com.tpstream.player.*
import com.tpstream.player.R
import com.tpstream.player.databinding.TpDownloadTrackSelectionDialogBinding
import com.tpstream.player.data.Video
import okio.IOException
import kotlin.math.roundToInt

internal typealias OnSubmitListener = (DownloadRequest,Video?) -> Unit

internal class DownloadResolutionSelectionSheet(
    val player: TpStreamPlayerImpl,
    parameters: TrackSelectionParameters,
) : BottomSheetDialogFragment(), VideoDownloadRequestCreationHandler.Listener {

    private var _binding: TpDownloadTrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    private val video = player.video
    private lateinit var videoDownloadRequestCreateHandler: VideoDownloadRequestCreationHandler
    var overrides: MutableMap<TrackGroup, TrackSelectionOverride> =
        parameters.overrides.toMutableMap()
    var isResolutionSelected = false
    private var trackGroups: MutableList<Tracks.Group> = mutableListOf()
    private var onSubmitListener: OnSubmitListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoDownloadRequestCreateHandler =
            VideoDownloadRequestCreationHandler(
                requireContext(),
                player
            )
        videoDownloadRequestCreateHandler.listener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TpDownloadTrackSelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDownloadRequestHandlerPrepared(isPrepared: Boolean, downloadHelper: DownloadHelper) {
        if (!this.isVisible) return
        prepareTrackGroup(downloadHelper)
        initializeDownloadResolutionSheet()
        showResolutions(isPrepared)
    }

    override fun onDownloadRequestHandlerPrepareError(downloadHelper: DownloadHelper, e: IOException) {
        dismiss()
    }

    private fun prepareTrackGroup(helper: DownloadHelper){
         val tracks = helper.getTracks(0)
        trackGroups = tracks.groups
    }

    private fun initializeDownloadResolutionSheet(){
        initializeTrackSelectionView()
        setOnClickListeners()
        configureBottomSheetBehaviour()
    }

    private fun showResolutions(isPrepared: Boolean){
        if (isPrepared) {
            binding.loadingProgress.visibility = View.GONE
            binding.resolutionLayout.visibility = View.VISIBLE
        }
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
            if (isResolutionSelected){
                val downloadRequest =
                    videoDownloadRequestCreateHandler.buildDownloadRequest(overrides)
                onSubmitListener?.invoke(downloadRequest,video)
                Toast.makeText(requireContext(), "Download Start", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Please choose download quality", Toast.LENGTH_SHORT).show()
            }
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
    ) : ArrayAdapter<TrackInfo>(context1, R.layout.tp_download_resulotion_data, dataSource) {
        private val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val resolution = getItem(position)!!
            var view = convertView
            if (convertView == null) {
                view = inflater.inflate(R.layout.tp_download_resulotion_data, parent, false)
            }
            val track = view!!.findViewById<CheckedTextView>(R.id.track_selecting)
            track.text = getResolution(resolution.format.height)

            track.isChecked = trackPosition == position

            if (track.isChecked){
                isResolutionSelected = true
            }

            view.findViewById<TextView>(R.id.track_size).text = getVideoSize(resolution)

            return view
        }

        private fun getVideoSize(trackInfo: TrackInfo): String {
            val mbps = (((trackInfo.format.bitrate).toFloat() / 8f / 1024f) / 1024f)
            val videoLengthInSecond = (player.getDuration().toFloat() / 1000f)
            return "${((mbps * videoLengthInSecond)).roundToInt()} MB"            //Mbps
        }

        private fun getResolution(height: Int): String {
            return when {
                height > 720 -> "Very High (${height}p)"
                height > 360 -> "High (${height}p)"
                height > 240 -> "Medium (${height}p)"
                else -> "Low (${height}p)"
            }
        }
    }

    inner class TrackInfo(val trackGroup: Tracks.Group, val trackIndex: Int) {
        val format: Format
            get() = trackGroup.getTrackFormat(trackIndex)
    }

    fun setOnSubmitListener(listener: OnSubmitListener) {
        onSubmitListener = listener
    }

}