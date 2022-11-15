package com.tpstream.player

import android.content.Context
import android.content.DialogInterface
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.ui.TrackNameProvider
import androidx.media3.ui.TrackSelectionView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tpstream.player.databinding.TrackSelectionDialogBinding


@UnstableApi
class VideoResolutionBottomSheet(parameters: DefaultTrackSelector.Parameters,
                                 private val trackGroups: List<Tracks.Group>,
                                var selectedResolution: ResolutionOptions) : BottomSheetDialogFragment(){

    private var _binding: TrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    var overrides: Map<TrackGroup, TrackSelectionOverride>
    var onClickListener: DialogInterface.OnClickListener? = null

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    init {
        overrides = parameters.overrides
    }

    constructor(trackSelector: DefaultTrackSelector, trackGroups: List<Tracks.Group>, selectedResolution: ResolutionOptions) : this(
        trackSelector.parameters,
        trackGroups,
        selectedResolution
    )

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
        super.onViewCreated(view, savedInstanceState)
        displayCurrentResolution()
        initializeList()
        configureBottomSheetBehaviour()
    }

    private fun initializeList() {
        val list = arrayListOf(
            Resolution("Auto (recommended)", "Adjusts to give you the best experience for your conditions", ResolutionOptions.AUTO),
            Resolution("Higher picture quality", "Uses more data", ResolutionOptions.HIGH),
            Resolution("Data saver", "Lower picture quality", ResolutionOptions.LOW),
            Resolution("Advanced", "Select a specific resolution", ResolutionOptions.ADVANCED)
        )
        val adapter = ResolutionAdapter(requireContext(), list, selectedResolution)
        val listView = requireView().findViewById<ListView>(R.id.listview)
        listView.adapter = adapter
        listView.setOnItemClickListener { adapterView, view, i, l ->
            val resolution = list[i]
            this@VideoResolutionBottomSheet.selectedResolution = resolution.option
            onClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }
    }

    private fun displayCurrentResolution() {
        val trackGroup = trackGroups.first { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }
        val currentResolution = requireView().findViewById<TextView>(R.id.current_resolution)
        if (overrides.isNotEmpty()) {
            val trackIndex = overrides.values.first().trackIndices.first()
            currentResolution.text = "${trackGroup.getTrackFormat(trackIndex).height}p"
        } else {
            currentResolution.text = "Auto"
        }
    }

    private fun configureBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.setTitle("Choose Quality")
        bottomSheetDialog.behavior.isDraggable = false
        bottomSheetDialog.behavior.isFitToContents = true
    }

    private fun initializeTrackSelectionView(view: View) {
        trackSelectionView = view.findViewById(androidx.media3.ui.R.id.exo_track_selection_view)
        trackSelectionView.setShowDisableOption(false)
        trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
        trackSelectionView.setAllowMultipleOverrides(false)
        trackSelectionView.setTrackNameProvider(ExoPlayerTrackNameProvider())
        trackSelectionView.init(trackGroups.filter { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }, false, overrides, null, this)
    }

    private fun setOnClickListeners() {
        binding.okButton.setOnClickListener {
            onClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        binding.cancelButton.setOnClickListener { dismiss() }
    }

    fun setAllowAdaptiveSelections(allow: Boolean) {
        allowAdaptiveSelections = allow
    }

    override fun onTrackSelectionChanged(
        isDisabled: Boolean,
        overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    ) {
        overrides.let {
            this.overrides = it
        }
    }
}

class ExoPlayerTrackNameProvider : TrackNameProvider {
    override fun getTrackName(format: Format): String {
        return when {
            format.height <= 240 -> "Very Low"
            format.height <= 360 -> "Low"
            format.height <= 480 -> "Medium"
            format.height <= 540 -> "High"
            format.height <= 720 -> "Very High"
            format.height <= 1080 -> "HD"
            else -> "${format.width}p"
        }
    }
}

object Util{
    fun getRendererIndex(trackType: Int, mappedTrackInfo: MappingTrackSelector.MappedTrackInfo): Int {
        for (i in 0 until mappedTrackInfo.rendererCount) {
            if (mappedTrackInfo.getRendererType(i) == trackType) {
                return i
            }
        }
        return -1
    }
}

data class Resolution(
    val title: String,
    val description: String,
    val option: ResolutionOptions
)

class ResolutionAdapter(context1: Context, dataSource: ArrayList<Resolution>, private val selectedResolution: ResolutionOptions): ArrayAdapter<Resolution>(context1, R.layout.resolution_data, dataSource) {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val resolution = getItem(position)!!
        var view = convertView
        if (convertView == null) {
            view =  inflater.inflate(R.layout.resolution_data, parent, false)
        }

        view!!.findViewById<TextView>(R.id.title).text = resolution.title
        view.findViewById<TextView>(R.id.description).text = resolution.description
        showOrHideCheckMark(view, resolution)
        return view
    }

    private fun showOrHideCheckMark(view: View, resolution: Resolution) {
        if (resolution.option == selectedResolution) {
            view.findViewById<ImageView>(R.id.auto_icon).visibility = View.VISIBLE
        } else {
            view.findViewById<ImageView>(R.id.auto_icon).visibility = View.GONE
        }
    }
}

enum class ResolutionOptions {
    AUTO {
        override fun getTrackSelectionParameter(
            context: Context,
            override: TrackSelectionOverride?
        ): TrackSelectionParameters {
            return TrackSelectionParameters.Builder(context).build()
        }
    },
    HIGH {
        override fun getTrackSelectionParameter(
            context: Context,
            override: TrackSelectionOverride?
        ): TrackSelectionParameters {
            return TrackSelectionParameters.Builder(context).setForceHighestSupportedBitrate(true).build()
        }
    },
    LOW {
        override fun getTrackSelectionParameter(
            context: Context,
            override: TrackSelectionOverride?
        ): TrackSelectionParameters {
            return TrackSelectionParameters.Builder(context).setForceLowestBitrate(true).build()
        }
    },
    ADVANCED {
        override fun getTrackSelectionParameter(
            context: Context,
            override: TrackSelectionOverride?
        ): TrackSelectionParameters {
            return TrackSelectionParameters.Builder(context).addOverride(override!!).build()
        }
    };

    abstract fun getTrackSelectionParameter(context: Context, override: TrackSelectionOverride?=null):TrackSelectionParameters
}