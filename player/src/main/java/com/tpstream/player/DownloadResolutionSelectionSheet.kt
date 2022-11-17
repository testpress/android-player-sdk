package com.tpstream.player

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.collect.ImmutableList
import com.tpstream.player.databinding.DownloadTrackSelectionDialogBinding
import com.tpstream.player.models.VideoInfo


typealias OnSubmitListener = (DownloadRequest) -> Unit


class DownloadResolutionSelectionSheet(
    private val parameters: DefaultTrackSelector.Parameters,
    private val trackGroups: List<Tracks.Group>,
    private val videoInfo: VideoInfo
) : BottomSheetDialogFragment() {

    private var _binding: DownloadTrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    var onClickListener: DialogInterface.OnClickListener? = null
    lateinit var overrides: MutableMap<TrackGroup, TrackSelectionOverride>

    private var onSubmitListener: OnSubmitListener? = null
    lateinit var videoDownloadRequestCreateHandler: VideoDownloadRequestCreationHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoDownloadRequestCreateHandler =
            VideoDownloadRequestCreationHandler(
                requireContext(),
                videoInfo
            )
        //videoDownloadRequestCreateHandler.listener = this
        overrides = parameters.overrides.toMutableMap()
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
        val adapter = Adapter(requireContext(), trackInfos, overrides)
        binding.listview.also {
            it.adapter = adapter
            it.setOnItemClickListener { _, _, index, _ ->
                val resolution = trackInfos[index]
                val mediaTrackGroup: TrackGroup = resolution.trackGroup.mediaTrackGroup
                overrides.clear()
                overrides[mediaTrackGroup] =
                    TrackSelectionOverride(mediaTrackGroup, ImmutableList.of(resolution.trackIndex))
                adapter.overrides = overrides
                //onClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                Log.d(
                    "TAG",
                    "initializeTrackSelectionView: 1 ${
                        overrides.map { t ->
                            t.value.trackIndices.get(0)
                        }
                    }"
                )
                //dismiss()
            }
        }
    }

    private fun setOnClickListeners() {
        binding.startDownload.setOnClickListener {
            if (::overrides.isInitialized) {

                val downloadRequest =
                    videoDownloadRequestCreateHandler.buildDownloadRequest(overrides.map { it.value }
                        .toList())

                DownloadService.sendAddDownload(
                    requireContext(),
                    VideoDownloadService::class.java,
                    downloadRequest,
                    false
                )
            }
            Toast.makeText(requireContext(), "Download Start", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.cancelDownload.setOnClickListener { dismiss() }

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

    inner class Adapter(
        context1: Context,
        dataSource: ArrayList<TrackInfo>,
        var overrides: Map<TrackGroup, TrackSelectionOverride>
    ) : ArrayAdapter<TrackInfo>(context1, R.layout.download_resulotion_data, dataSource) {
        private val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private var values = overrides.values.map { trackSelection ->
            trackSelection.trackIndices[0]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val resolution = getItem(position)!!
            var view = convertView
            if (convertView == null) {
                view = inflater.inflate(R.layout.download_resulotion_data, parent, false)
            }
            val track = view!!.findViewById<CheckedTextView>(R.id.track_selecting)
            track.text = "${resolution.format.height}p"
            track.isChecked = resolution.trackIndex in values

            notifyDataSetChanged()

            Log.d("TAG", "getView: ${resolution.trackIndex in values}")

            return view
        }
    }

    inner class TrackInfo(val trackGroup: Tracks.Group, val trackIndex: Int) {
        val format: Format
            get() = trackGroup.getTrackFormat(trackIndex)
    }

//    override fun onDownloadRequestHandlerPrepared(
//        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
//        rendererIndex: Int,
//        overrides: List<DefaultTrackSelector.SelectionOverride>
//    ) {
//
//    }
//
//    override fun onDownloadRequestHandlerPrepareError(helper: DownloadHelper, e: IOException) {
//
//    }

}