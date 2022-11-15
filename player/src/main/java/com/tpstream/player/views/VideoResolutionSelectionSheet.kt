package com.tpstream.player.views

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tpstream.player.R
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.databinding.TrackSelectionDialogBinding


@UnstableApi
class VideoResolutionSelectionSheet(
    private val player: TpStreamPlayer,
    var selectedResolution: ResolutionOptions
) : BottomSheetDialogFragment(){

    private var _binding: TrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    var onClickListener: DialogInterface.OnClickListener? = null

    companion object {
        const val TAG = "ModalBottomSheet"
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
        super.onViewCreated(view, savedInstanceState)
        configureBottomSheetBehaviour()
        initializeList()
        displayCurrentResolution()
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
        val list = getResolutions()

        binding.listview.also {
            it.adapter = ResolutionAdapter(requireContext(), list, selectedResolution)
            it.setOnItemClickListener { adapterView, view, i, l ->
                val resolution = list[i]
                this@VideoResolutionSelectionSheet.selectedResolution = resolution.option
                onClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                dismiss()
            }
        }
    }

    private fun getResolutions(): ArrayList<Resolution> {
        return arrayListOf(
            Resolution("Auto (recommended)", "Adjusts to give you the best experience for your conditions", ResolutionOptions.AUTO),
            Resolution("Higher picture quality", "Uses more data", ResolutionOptions.HIGH),
            Resolution("Data saver", "Lower picture quality", ResolutionOptions.LOW),
            Resolution("Advanced", "Select a specific resolution", ResolutionOptions.ADVANCED)
        )
    }

    private fun configureBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.behavior.isDraggable = true
        bottomSheetDialog.behavior.isFitToContents = true
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
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

class ResolutionAdapter(context1: Context, dataSource: ArrayList<Resolution>, private val selectedResolution: ResolutionOptions): ArrayAdapter<Resolution>(context1,
    R.layout.resolution_data, dataSource) {
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