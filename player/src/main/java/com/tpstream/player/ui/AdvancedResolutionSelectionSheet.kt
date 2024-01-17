package com.tpstream.player.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.tpstream.player.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.collect.ImmutableList
import com.tpstream.player.R
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.databinding.TpTrackSelectionDialogBinding

internal class AdvancedResolutionSelectionSheet(
    private val player: TpStreamPlayer,
    parameters: TrackSelectionParameters,
): BottomSheetDialogFragment() {

    private var _binding: TpTrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    var onClickListener: DialogInterface.OnClickListener? = null
    var overrides: MutableMap<TrackGroup, TrackSelectionOverride> = parameters.overrides.toMutableMap()
    private val trackGroups = player.getCurrentTrackGroups()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        val trackInfos = getTrackInfos()
        binding.listview.also { it ->
            it.adapter = Adapter(requireContext(), trackInfos, overrides)
            it.setOnItemClickListener { _, _, index, _ ->
                val resolution = trackInfos[index]
                val mediaTrackGroup: TrackGroup = resolution.trackGroup.mediaTrackGroup
                overrides.clear()
                overrides[mediaTrackGroup] = TrackSelectionOverride(mediaTrackGroup, ImmutableList.of(resolution.trackIndex))
                onClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                dismiss()
            }
        }
    }

    private fun getTrackInfos(): ArrayList<TrackInfo> {
        if (trackGroups.none { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }) {
            return arrayListOf()
        }

        val trackGroup = trackGroups.first { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }
        return if (player.getMaxResolution() == null) {
            getAllTracks(trackGroup)
        } else {
            getTracksBelowMaxResolution(trackGroup)
        }
    }

    private fun getAllTracks(trackGroup: TracksGroup): ArrayList<TrackInfo> {
        return (0 until trackGroup.length).mapTo(arrayListOf()) { TrackInfo(trackGroup, it) }
    }

    private fun getTracksBelowMaxResolution(trackGroup: TracksGroup): ArrayList<TrackInfo> {
        val maxResolution = player.getMaxResolution()!!

        return (0 until trackGroup.length)
            .filter { maxResolution >= trackGroup.mediaTrackGroup.getFormat(it).height }
            .mapTo(arrayListOf()) { TrackInfo(trackGroup, it) }
    }

    private fun configureBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.behavior.isDraggable = true
        bottomSheetDialog.behavior.isFitToContents = true
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    inner class Adapter(context1: Context, dataSource: ArrayList<TrackInfo>, overrides: Map<TrackGroup, TrackSelectionOverride>): ArrayAdapter<TrackInfo>(context1,
        R.layout.tp_resolution_data, dataSource) {
        private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val values = overrides.values.map { trackSelection ->
            trackSelection.trackIndices[0]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val resolution = getItem(position)!!
            var view = convertView
            if (convertView == null) {
                view =  inflater.inflate(R.layout.tp_resolution_data, parent, false)
            }
            view!!.findViewById<TextView>(R.id.title).text = "${resolution.format.height}p"
            showOrHideCheckMark(view, resolution)
            return view
        }

        private fun showOrHideCheckMark(view: View, resolution: TrackInfo) {
            if (resolution.trackIndex in values) {
                view.findViewById<ImageView>(R.id.auto_icon).visibility = View.VISIBLE
            } else {
                view.findViewById<ImageView>(R.id.auto_icon).visibility = View.GONE
            }
        }
    }

    inner class TrackInfo(val trackGroup: TracksGroup, val trackIndex: Int) {
        val format: Format
            get() = trackGroup.getTrackFormat(trackIndex)
    }
}
