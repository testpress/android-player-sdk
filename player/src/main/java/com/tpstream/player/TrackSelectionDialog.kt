package com.tpstream.player

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import androidx.media3.common.*
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.ui.TrackNameProvider
import androidx.media3.ui.TrackSelectionView
import com.tpstream.player.databinding.TrackSelectionDialogBinding


class TrackSelectionDialog(
    parameters: DefaultTrackSelector.Parameters,
    private val trackGroups: List<Tracks.Group>
) : DialogFragment(),
    TrackSelectionView.TrackSelectionListener {
    private var _binding: TrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackSelectionView: TrackSelectionView
    private var allowAdaptiveSelections = false
    var overrides: Map<TrackGroup, TrackSelectionOverride>
    var onClickListener: DialogInterface.OnClickListener? = null


    init {
        overrides = parameters.overrides
    }

    constructor(trackSelector: DefaultTrackSelector, trackGroups: List<Tracks.Group>) : this(
        trackSelector.parameters,
        trackGroups
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
        initializeTrackSelectionView(view)
        setOnClickListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(requireActivity(), android.R.style.Theme_Material_Dialog_NoActionBar)
        dialog.setTitle("Choose Quality")
        return dialog
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