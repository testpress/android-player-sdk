package com.tpstream.player.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.collect.ImmutableList
import com.tpstream.player.*
import com.tpstream.player.R
import com.tpstream.player.databinding.TpDownloadTrackSelectionDialogBinding
import com.tpstream.player.data.Asset
import com.tpstream.player.data.Track
import com.tpstream.player.data.source.network.TPStreamsNetworkAsset
import com.tpstream.player.offline.VideoDownloadRequestCreationHandler
import com.tpstream.player.util.DeviceUtil
import okio.IOException
import kotlin.math.roundToInt

internal typealias OnSubmitListener = (DownloadRequest, Asset?) -> Unit

internal class DownloadResolutionSelectionSheet : BottomSheetDialogFragment(), VideoDownloadRequestCreationHandler.Listener {

    private var _binding: TpDownloadTrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var videoDownloadRequestCreateHandler: VideoDownloadRequestCreationHandler
    private lateinit var overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    private lateinit var asset: Asset
    private lateinit var params: TpInitParams
    var isResolutionSelected = false
    private var trackGroups: MutableList<TracksGroup> = mutableListOf()
    private var onSubmitListener: OnSubmitListener? = null
    private var codec: List<DeviceUtil.CodecDetails> = emptyList()

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

    fun initializeVideoDownloadRequestCreateHandler(context: Context, asset: Asset, params: TpInitParams) {
        codec = DeviceUtil.getAvailableAVCCodecs()
        this.asset = asset
        this.params = params
        videoDownloadRequestCreateHandler =
            VideoDownloadRequestCreationHandler(
                context,
                asset = asset,
                params = params
            )
        videoDownloadRequestCreateHandler.listener = this
    }

    override fun onDownloadRequestHandlerPrepared(isPrepared: Boolean, downloadHelper: DownloadHelper) {
        if (!this.isVisible) return
        prepareTrackGroup(downloadHelper)
        prepareOverride()
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

    private fun prepareOverride(){
        overrides = DownloadHelper.getDefaultTrackSelectorParameters(requireContext()).overrides.toMutableMap()
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
                overrides.clear()
                val resolution = trackInfos[index]
                val videoTrackGroup: TrackGroup = resolution.videoTrackGroup.mediaTrackGroup
                // Add selected Video track
                overrides[videoTrackGroup] =
                    TrackSelectionOverride(videoTrackGroup, resolution.trackIndex)
                // Add selected Audio track only if multiple track available
                // for non- drm video multiple track are not available
                val audioTrackGroup: TrackGroup = resolution.audioTrackGroup.mediaTrackGroup
                if (audioTrackGroup.length > 1){
                    overrides[audioTrackGroup] =
                        TrackSelectionOverride(audioTrackGroup, resolution.trackIndex)
                }
            }
        }
    }

    private fun setOnClickListeners() {
        binding.startDownload.setOnClickListener {
            if (isResolutionSelected){
                val downloadRequest =
                    videoDownloadRequestCreateHandler.buildDownloadRequest(overrides)
                onSubmitListener?.invoke(downloadRequest,asset)
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

        val videoTracksGroup = trackGroups.first { it.mediaTrackGroup.type == C.TRACK_TYPE_VIDEO }
        val audioTrackGroup = trackGroups.first { it.mediaTrackGroup.type == C.TRACK_TYPE_AUDIO }
        for (trackIndex in 0 until videoTracksGroup.length) {
            trackInfos.add(TrackInfo(videoTracksGroup, audioTrackGroup, trackIndex))
        }

        val supportedTrackInfos = trackInfos.filterSupportedTracks()
        return supportedTrackInfos
    }

    private fun ArrayList<DownloadResolutionSelectionSheet.TrackInfo>.filterSupportedTracks(): ArrayList<DownloadResolutionSelectionSheet.TrackInfo> {
        return filterTo(ArrayList()) { trackInfo ->
            val resolutionHeight = trackInfo.videoFormat.height
            // Keep the track if codec support
            isCodecSupported(resolutionHeight)
        }
    }

    private fun isCodecSupported(resolutionHeight: Int): Boolean {
        val selectedCodecDetails = getRelevantCodecDetails()
        // Check if the track resolution is supported by the selected codec capabilities
        return selectedCodecDetails?.let { codecCapabilities ->
            when (resolutionHeight) {
                in 0..1079 -> true // Assuming anything below 1080p is supported
                1080 -> codecCapabilities.is1080pSupported
                2160 -> codecCapabilities.is4KSupported
                else -> false // Anything above 4K is not supported
            }
        } ?: true
    }

    private fun getRelevantCodecDetails(): DeviceUtil.CodecDetails? {
        val isDrmProtected = asset.video.isDrmProtected == true

        // Function to get the codec based on resolution support
        fun selectBestCodec(codecs: List<DeviceUtil.CodecDetails>): DeviceUtil.CodecDetails? {
            return codecs.maxByOrNull {
                when {
                    it.is4KSupported -> 2
                    it.is1080pSupported -> 1
                    else -> 0
                }
            }
        }

        return if (isDrmProtected) {
            // For DRM-protected content, choose secure codec if available; otherwise, fallback to non-secure codec
            codec.firstOrNull { it.isSecure }
                ?: selectBestCodec(codec.filter { !it.isSecure }) // For L3 devices, where secure codecs are unavailable, fallback to non-secure codec
        } else {
            // For non-DRM content, return the non-secure codec with maximum resolution support
            selectBestCodec(codec.filter { !it.isSecure })
        }
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
            track.text = getResolution(resolution.videoFormat.height)

            track.isChecked = trackPosition == position

            if (track.isChecked){
                isResolutionSelected = true
            }

            view.findViewById<TextView>(R.id.track_size).text = getTotalMediaSize(resolution)

            return view
        }

        private fun getTotalMediaSize(trackInfo: TrackInfo): String {
            val track = asset.video.tracks?.firstOrNull { it.type == "Playlist" }
            val playlistSize = getPlaylistSize(track, trackInfo)
            val size = playlistSize ?: (getVideoSizeInMB(trackInfo) + getAudioSizeInMB(trackInfo))
            return "$size MB"
        }

        private fun getPlaylistSize(track: Track?, trackInfo: TrackInfo): Long? {
            return track?.playlists
                ?.filter { // Filter based on DRM protection
                    if (asset.video.isDrmProtected == true) {
                        it.name.contains("dash")
                    } else {
                        true // No filtering for non-DRM, allow all playlists
                    }
                }
                ?.firstOrNull { it.height == trackInfo.videoFormat.height }
                ?.bytes?.let { (it / 1024L) / 1024L }
        }

        private fun getVideoSizeInMB(trackInfo: TrackInfo): Int {
            val mbps = (((trackInfo.videoFormat.bitrate).toFloat() / 8f / 1024f) / 1024f)
            val videoLengthInSecond = asset.video.duration
            return (mbps * videoLengthInSecond).roundToInt()            //Mbps
        }

        private fun getAudioSizeInMB(trackInfo: TrackInfo): Int {
            if (trackInfo.audioFormat == null) return 0
            val mbps = (((trackInfo.audioFormat!!.bitrate).toFloat() / 8f / 1024f) / 1024f)
            val videoLengthInSecond = asset.video.duration
            return (mbps * videoLengthInSecond).roundToInt()           //Mbps
        }

        private fun getResolution(height: Int): String {
            return when  {
                height > 1080 -> "Ultra High (${height}p)"
                height in 721..1080 -> "Very High (${height}p)"
                height in 481..720 -> "High (${height}p)"
                height in 361..480 -> "Medium (${height}p)"
                height in 241..360 -> "Low (${height}p)"
                else -> "Very Low (${height}p)"
            }
        }
    }

    inner class TrackInfo(
        val videoTrackGroup: TracksGroup,
        val audioTrackGroup: TracksGroup,
        val trackIndex: Int
    ) {
        val videoFormat: Format
            get() = videoTrackGroup.getTrackFormat(trackIndex)
        val audioFormat: Format?
            get() {
                return try {
                    audioTrackGroup.getTrackFormat(trackIndex)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    // If an invalid track index is encountered (e.g.,
                    // for non-DRM videos where only one audio track is available),
                    // return the first audio track format as a fallback.
                    audioTrackGroup.getTrackFormat(0)
                }
            }
    }

    fun setOnSubmitListener(listener: OnSubmitListener) {
        onSubmitListener = listener
    }

}