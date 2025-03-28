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
    private var selectedCodecDetails: DeviceUtil.CodecDetails? = null
    private val maxResolution: Int? by lazy { player.getMaxResolution() }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        selectedCodecDetails = player.codecs.firstOrNull { it.isSelected }
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
            val filteredTracksInfo = getTracksInfo(group).filterSupportedTracks()
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

    private fun ArrayList<TrackInfo>.filterSupportedTracks(): ArrayList<TrackInfo> {
        return filterTo(ArrayList()) { trackInfo ->
            val resolutionHeight = trackInfo.format.height
            // Keep the track if it meets both resolution and codec support criteria
            isResolutionWithinMaxResolution(resolutionHeight) && isCodecSupported(resolutionHeight)
        }
    }

    private fun isResolutionWithinMaxResolution(resolutionHeight: Int): Boolean {
        // Check if the track resolution is within the maximum allowed resolution
        return maxResolution?.let { resolutionHeight <= it } ?: true
    }

    private fun isCodecSupported(resolutionHeight: Int): Boolean {
        // Check if the track resolution is supported by the codec capabilities
        return selectedCodecDetails?.let { codecCapabilities ->
            when (resolutionHeight) {
                in 0 .. 1079 -> true // Assuming anything below 1080p is supported
                1080 -> codecCapabilities.is1080pSupported
                2160 -> codecCapabilities.is4KSupported
                else -> false // Anything above 4K is not supported
            }
        } ?: true
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
