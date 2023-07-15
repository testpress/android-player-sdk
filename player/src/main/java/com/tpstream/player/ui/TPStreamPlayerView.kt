package com.tpstream.player.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.media3.ui.PlayerView
import com.tpstream.player.EncryptionKeyRepository
import com.tpstream.player.R
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerImpl
import com.tpstream.player.data.VideoRepository
import com.tpstream.player.data.source.local.DownloadStatus
import com.tpstream.player.databinding.TpStreamPlayerViewBinding
import com.tpstream.player.offline.DownloadTask
import com.tpstream.player.ui.viewmodel.VideoViewModel
import com.tpstream.player.util.ImageSaver

class TPStreamPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ViewModelStoreOwner {
    private val binding: TpStreamPlayerViewBinding =
        TpStreamPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var playerView: PlayerView = binding.playerView
    private lateinit var player: TpStreamPlayerImpl
    private var downloadButton: Button? = null
    private var downloadState : DownloadStatus? = null
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var viewModelStore: ViewModelStore

    init {
        registerDownloadListener()
        initializeViewModel()
    }

    private fun registerDownloadListener() {
        downloadButton = playerView.findViewById(R.id.exo_download)
        downloadButton?.setOnClickListener {
            onDownloadButtonClick()
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

    fun setPlayer(player: TpStreamPlayer) {
        this.player = player as TpStreamPlayerImpl
        playerView.player = this.player.exoPlayer
    }

    fun getPlayer(): TpStreamPlayer {
        return player
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
                downloadResolutionSelectionSheet.show((context as AppCompatActivity).supportFragmentManager, "DownloadSelectionSheet")
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

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }
}
