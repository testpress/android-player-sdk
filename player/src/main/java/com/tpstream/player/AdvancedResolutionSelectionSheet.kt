package com.tpstream.player

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.*
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.collect.ImmutableList
import com.tpstream.player.databinding.TrackSelectionDialogBinding

class AdvancedResolutionSelectionSheet(
    parameters: DefaultTrackSelector.Parameters, private val trackGroups: List<Tracks.Group>
): BottomSheetDialogFragment() {

    private var _binding: TrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    var onClickListener: DialogInterface.OnClickListener? = null
    var overrides: MutableMap<TrackGroup, TrackSelectionOverride>

    init {
        overrides = parameters.overrides.toMutableMap()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TrackSelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeList()
        configureBottomSheetBehaviour()
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
        val trackGroup = trackGroups.first { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }
        val trackInfos = arrayListOf<TrackInfo>()
        for (trackIndex in 0 until trackGroup.length) {
            trackInfos.add(TrackInfo(trackGroup, trackIndex))
        }
        return trackInfos
    }

    private fun configureBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.setTitle("Choose Quality")
        bottomSheetDialog.behavior.isDraggable = false
        bottomSheetDialog.behavior.isFitToContents = true
    }

    inner class Adapter(context1: Context, dataSource: ArrayList<TrackInfo>, overrides: Map<TrackGroup, TrackSelectionOverride>): ArrayAdapter<TrackInfo>(context1, R.layout.resolution_data, dataSource) {
        private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val values = overrides.values.map { trackSelection ->
            trackSelection.trackIndices[0]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val resolution = getItem(position)!!
            var view = convertView
            if (convertView == null) {
                view =  inflater.inflate(R.layout.resolution_data, parent, false)
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

    inner class TrackInfo(val trackGroup: Tracks.Group, val trackIndex: Int) {
        val format: Format
            get() = trackGroup.getTrackFormat(trackIndex)
    }
}