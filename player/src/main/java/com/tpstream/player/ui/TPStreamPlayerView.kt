package com.tpstream.player.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.tpstream.player.*
import com.tpstream.player.EncryptionKeyRepository
import com.tpstream.player.TpStreamPlayerImpl
import com.tpstream.player.data.VideoRepository
import com.tpstream.player.data.source.local.DownloadStatus
import com.tpstream.player.databinding.TpStreamPlayerViewBinding
import com.tpstream.player.offline.DownloadTask
import com.tpstream.player.ui.viewmodel.VideoViewModel
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.util.MarkerState
import com.tpstream.player.util.getPlayedStatusArray
import java.util.*
import java.util.concurrent.TimeUnit

class TPStreamPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ViewModelStoreOwner {
    private val binding: TpStreamPlayerViewBinding =
        TpStreamPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var playerView: PlayerView = binding.playerView
    private lateinit var player: TpStreamPlayerImpl
    private var downloadButton: ImageButton? = null
    private var resolutionButton : ImageButton? = null
    private var downloadState : DownloadStatus? = null
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var viewModelStore: ViewModelStore
    private var selectedResolution = ResolutionOptions.AUTO
    private lateinit var simpleResolutionSheet:SimpleResolutionSelectionSheet
    private lateinit var advancedResolutionSheet:AdvancedResolutionSelectionSheet
    private val seekBar get() = binding.playerView.findViewById<DefaultTimeBar>(ExoplayerProgressBarID.exo_progress)
    private var seekBarListener: OnScrubListener? = null
    private var markers: LinkedHashMap<Long, MarkerState>? = null
    private var animator: ObjectAnimator? = null

    init {
        registerDownloadListener()
        registerResolutionChangeListener()
        initializeViewModel()
    }

    private fun registerDownloadListener() {
        downloadButton = playerView.findViewById(R.id.exo_download)
        downloadButton?.setOnClickListener {
            onDownloadButtonClick()
        }
    }

    private fun registerResolutionChangeListener() {
        resolutionButton = playerView.findViewById(R.id.exo_resolution)
        resolutionButton?.setOnClickListener {
            onResolutionButtonClick()
        }
    }

    private fun initializeViewModel() {
        viewModelStore = ViewModelStore()
        videoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VideoViewModel(VideoRepository(context)) as T
            }
        }).get(VideoViewModel::class.java)
    }

    private fun onDownloadButtonClick() {
        when (downloadState) {
            DownloadStatus.COMPLETE -> {
                Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
            }
            DownloadStatus.DOWNLOADING -> {
                Toast.makeText(context, "Downloading", Toast.LENGTH_SHORT).show()
            }
            else -> {
                EncryptionKeyRepository(context).fetchAndStore(
                    player.params,
                    player.video?.url!!
                )
                val downloadResolutionSelectionSheet = DownloadResolutionSelectionSheet(
                    player,
                    player.getTrackSelectionParameters(),
                )
                downloadResolutionSelectionSheet.show((context as FragmentActivity).supportFragmentManager, "DownloadSelectionSheet")
                downloadResolutionSelectionSheet.setOnSubmitListener { downloadRequest, video ->
                    DownloadTask(context).start(downloadRequest)
                    video?.videoId = player.params.videoId!!
                    ImageSaver(context).save(
                        video?.thumbnail!!,
                        video.videoId
                    )
                    videoViewModel.insert(video)
                }
            }
        }
    }

    private fun onResolutionButtonClick() {
        if (downloadState == DownloadStatus.COMPLETE) {
            Toast.makeText(context, "Quality Unavailable", Toast.LENGTH_SHORT).show()
        } else {
            initializeResolutionSelectionSheets()
            simpleResolutionSheet.show(
                (context as FragmentActivity).supportFragmentManager,
                SimpleResolutionSelectionSheet.TAG
            )
        }
    }

    private fun initializeResolutionSelectionSheets() {
        simpleResolutionSheet = SimpleResolutionSelectionSheet(player!!, selectedResolution)
        advancedResolutionSheet = AdvancedResolutionSelectionSheet(player!!, player!!.getTrackSelectionParameters())
        simpleResolutionSheet.onClickListener = onSimpleResolutionSelection()
        advancedResolutionSheet.onClickListener = onAdvancedVideoResolutionSelection()
    }

    private fun onSimpleResolutionSelection() = DialogInterface.OnClickListener { p0, p1 ->
        selectedResolution = simpleResolutionSheet.selectedResolution
        if (simpleResolutionSheet.selectedResolution == ResolutionOptions.ADVANCED) {
            advancedResolutionSheet.show((context as FragmentActivity).supportFragmentManager, "AdvancedSheet")
            return@OnClickListener
        } else {
            val parameters = simpleResolutionSheet.selectedResolution.getTrackSelectionParameter(
                context,
                null
            )
            player?.setTrackSelectionParameters(parameters)
        }
    }

    private fun onAdvancedVideoResolutionSelection() = DialogInterface.OnClickListener { p0, p1 ->
        val mappedTrackInfo = (player?.getTrackSelector() as DefaultTrackSelector).currentMappedTrackInfo
        mappedTrackInfo?.let {
            val rendererIndex = Util.getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
            if (advancedResolutionSheet.overrides.isNotEmpty()) {
                val params = TrackSelectionParametersBuilder(context)
                    .clearOverridesOfType(rendererIndex)
                    .addOverride(advancedResolutionSheet.overrides.values.elementAt(0))
                    .build()
                player?.setTrackSelectionParameters(params)
            }
        }
    }

    fun getPlayer(): TpStreamPlayer = player

    fun setPlayer(player: TpStreamPlayer) {
        this.player = player as TpStreamPlayerImpl
        playerView.player = this.player.exoPlayer
        initializeLoadCompleteListener()
        initializeMarkerListener()
    }

    private fun initializeLoadCompleteListener() {
        player.setLoadCompleteListener {
            (context as FragmentActivity).runOnUiThread {
                if (player.params.isDownloadEnabled) {
                    downloadButton?.isVisible = true
                    updateDownloadButtonImage()
                }
            }
        }
    }

    private fun initializeMarkerListener() {
        player.setMarkerListener { time ->
            markers?.updatePlayedMarker(time)
            playerView.setExtraAdGroupMarkers(
                markers?.keys?.toLongArray(),
                markers?.values?.getPlayedStatusArray()
            )
        }
    }

    private fun LinkedHashMap<Long, MarkerState>.updatePlayedMarker(time: Long) {
        if (this[time]?.shouldDeleteAfterDelivery == true) {
            this[time]?.isPlayed = true
        }
    }

    private fun updateDownloadButtonImage(){
        videoViewModel.get(player.video?.videoId!!).observe(context as FragmentActivity) { video ->
            downloadState = when (video?.downloadState) {
                DownloadStatus.DOWNLOADING ->{
                    downloadButton?.setImageResource(R.drawable.ic_baseline_downloading_24)
                    DownloadStatus.DOWNLOADING
                }
                DownloadStatus.COMPLETE ->{
                    downloadButton?.setImageResource(R.drawable.ic_baseline_file_download_done_24)
                    DownloadStatus.COMPLETE
                }
                else -> {
                    downloadButton?.setImageResource(R.drawable.ic_baseline_download_for_offline_24)
                    null
                }
            }
        }
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    fun showFastForwardButton() = playerView.setShowFastForwardButton(true)

    fun hideFastForwardButton() = playerView.setShowFastForwardButton(false)

    fun showRewindButton() = playerView.setShowRewindButton(true)

    fun hideRewindButton() = playerView.setShowRewindButton(false)

    fun showResolutionButton() {
        resolutionButton?.isVisible = true
    }

    fun hideResolutionButton() {
        resolutionButton?.isVisible = false
    }

    fun showController() {
        playerView.showController()
    }

    fun hideController() {
        playerView.hideController()
    }

    fun useController(useController: Boolean) {
        playerView.useController = useController
    }

    fun setFullscreenButtonClickListener(listener: FullscreenButtonClickListener?) {
        playerView.setFullscreenButtonClickListener(listener)
    }

    fun enableSeekBar(){
        removeSeekBarListener()
    }

    fun disableSeekBar(message: String = "Seek option is Disable") {
        removeSeekBarListener()
        seekBarListener = object : OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                timeBar.setEnabled(false)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {}
            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {}
        }
        seekBar.addListener(seekBarListener!!)
    }

    private fun removeSeekBarListener() {
        if (seekBarListener != null) {
            seekBar.removeListener(seekBarListener!!)
            seekBarListener = null
        }
    }

    fun setMarkers(
        timesInSeconds: LongArray,
        @ColorInt markerColor: Int = Color.YELLOW,
        deleteAfterDelivery: Boolean = true
    ) {
        // Check if markers have already been generated. If not, generate new markers.
        //If the app comes from the background, the markers will be already available,
        //and there is no need to generate new markers again.
        if (markers == null) {
            markers = generateMarkers(timesInSeconds, deleteAfterDelivery)
        }
        addMarkersToPlayer()
        addMarkerToPlayerView(markerColor)
    }

    private fun generateMarkers(
        timesInSeconds: LongArray,
        deleteAfterDelivery: Boolean = true
    ): LinkedHashMap<Long, MarkerState> {
        val timesInMs = timesInSeconds.map { TimeUnit.SECONDS.toMillis(it) }.toLongArray()
        return timesInMs.associateWith {
            MarkerState(false, deleteAfterDelivery)
        }.toMap(linkedMapOf())
    }

    private fun addMarkersToPlayer() {
        markers?.map {
            if (!it.value.isPlayed) {
                player.addMarker(it.key, it.value.shouldDeleteAfterDelivery)
            }
        }
    }

    private fun addMarkerToPlayerView(markerColor: Int) {
        playerView.setExtraAdGroupMarkers(
            markers?.keys?.toLongArray(),
            markers?.values?.getPlayedStatusArray()
        )
        seekBar.setAdMarkerColor(markerColor)
    }

    fun enableWaterMark(text: String, @ColorInt color: Int) {
        binding.watermarkView.isVisible = true
        binding.watermarkView.text = text
        binding.watermarkView.setTextColor(ColorStateList.valueOf(color))
        // Since the width of the TextView is not immediately available after it is created or modified,
        // we need to use a layout change listener to wait for the layout to be updated.
        binding.watermarkView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                view: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                // We remove the layout change listener to avoid unnecessary future calls.
                binding.watermarkView.removeOnLayoutChangeListener(this)
                binding.watermarkView.startAnimation()
            }
        })
    }

    fun disableWaterMark() {
        animator?.cancel()
        binding.watermarkView.isVisible = false
    }

    private fun TextView.startAnimation() {
        this.x = -this.width.toFloat()
        val screenWidth = resources.displayMetrics.widthPixels

        animator = ObjectAnimator.ofFloat(
            this,
            "translationX",
            -this.width.toFloat(),
            screenWidth.toFloat()
        )

        animator?.let {
            it.duration = 10000
            it.repeatMode = ObjectAnimator.REVERSE
            it.repeatCount = ObjectAnimator.INFINITE
            it.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {}
                override fun onAnimationEnd(p0: Animator?) {}
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationRepeat(p0: Animator?) {
                    this@startAnimation.y =
                        Random().nextFloat() * (playerView.height - this@startAnimation.height)
                }
            })
            it.start()
        }
    }

}
