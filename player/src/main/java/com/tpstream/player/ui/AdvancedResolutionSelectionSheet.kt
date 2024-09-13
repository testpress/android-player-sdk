package com.tpstream.player.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tpstream.player.*
import com.tpstream.player.databinding.TpTrackSelectionDialogBinding
import com.tpstream.player.util.DeviceUtil

internal class AdvancedResolutionSelectionSheet(
    private val player: TpStreamPlayerImpl
): BottomSheetDialogFragment() {

    private var _binding: TpTrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    var onAdvanceResolutionClickListener: OnAdvanceResolutionClickListener? = null
    private val tracksGroups = player.getCurrentTrackGroups()
    private var codecDetails: DeviceUtil.CodecDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        codecDetails = player.codecs.firstOrNull { it.isSelected }
        _binding = TpTrackSelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBottomSheetBehaviour()
        displayCurrentResolution()
        initializeList()
    }

    private fun displayCurrentResolution() {
        val currentResolution = if (player.getVideoFormat() != null) {
            "${player.getVideoFormat()!!.height}p"
        } else {
            "Auto"
        }
        binding.currentResolution.text = currentResolution
    }

    private fun initializeList() {
        val trackGroup = tracksGroups.firstOrNull { it.type == C.TRACK_TYPE_VIDEO }
        trackGroup?.let { group ->
            val filteredTracksInfo = getTracksInfo(group).filterTracksInfo()
            setupListView(filteredTracksInfo)
        } ?: dismiss()
    }

    private fun getTracksInfo(trackGroup: TracksGroup): ArrayList<TrackInfo> {
        val isMultipleTrackSelected = isMultipleTrackSelected(trackGroup)

        return (0 until trackGroup.length).mapTo(ArrayList()) { trackIndex ->
            TrackInfo(
                trackGroup.getTrackFormat(trackIndex),
                !isMultipleTrackSelected && trackGroup.isTrackSelected(trackIndex)
            )
        }
    }
    private fun ArrayList<TrackInfo>.filterTracksInfo(): ArrayList<TrackInfo> {
        val maxResolution = player.getMaxResolution()

        return filterTo(ArrayList()) { trackInfo ->
            val height = trackInfo.format.height

            // Check if the track resolution is within the maximum allowed resolution
            val resolutionSupport = maxResolution?.let { height <= it } ?: true

            // Check if the track resolution is supported by the codec capabilities
            val codecSupport = codecDetails?.let { codecCapabilities ->
                when (height) {
                    1080 -> codecCapabilities.is1080pSupported
                    2160 -> codecCapabilities.is4KSupported
                    else -> true // Allow all resolutions lower than 720p
                }
            } ?: true

            // Keep the track if it meets both resolution and codec support criteria
            resolutionSupport && codecSupport
        }
    }

    private fun setupListView(tracksInfo: ArrayList<TrackInfo>) {
        binding.listview.apply {
            adapter = Adapter(requireContext(), tracksInfo)
            setOnItemClickListener { _, _, index, _ ->
                onAdvanceResolutionClickListener?.onClick(index)
                dismiss()
            }
        }
    }
    private fun isMultipleTrackSelected(trackGroup: TracksGroup): Boolean {
        return (0 until trackGroup.length).count { trackGroup.isTrackSelected(it) } > 1
    }

    private fun configureBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.behavior.isDraggable = true
        bottomSheetDialog.behavior.isFitToContents = true
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    inner class Adapter(context1: Context, dataSource: ArrayList<TrackInfo>) :
        ArrayAdapter<TrackInfo>(
            context1,
            R.layout.tp_resolution_data, dataSource
        ) {
        private val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val trackInfo = getItem(position)!!
            var view = convertView
            if (convertView == null) {
                view = inflater.inflate(R.layout.tp_resolution_data, parent, false)
            }
            view!!.findViewById<TextView>(R.id.title).text = "${trackInfo.format.height}p"
            showOrHideCheckMark(view, trackInfo)
            return view
        }

        private fun showOrHideCheckMark(view: View, trackInfo: TrackInfo) {
            if (trackInfo.isSelected) {
                view.findViewById<ImageView>(R.id.auto_icon).visibility = View.VISIBLE
            } else {
                view.findViewById<ImageView>(R.id.auto_icon).visibility = View.GONE
            }
        }
    }

    interface OnAdvanceResolutionClickListener {
        fun onClick(index: Int)
    }

    data class TrackInfo(val format: Format, val isSelected: Boolean)
}
