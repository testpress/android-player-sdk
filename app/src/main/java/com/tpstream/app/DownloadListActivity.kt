package com.tpstream.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tpstream.app.databinding.ActivityDownloadListBinding
import com.tpstream.app.databinding.DownloadItemBinding
import com.tpstream.player.TpInitParams
import com.tpstream.player.models.Video
import com.tpstream.player.models.DownloadStatus
import com.tpstream.player.models.getLocalThumbnail

class DownloadListActivity : AppCompatActivity() {

    private lateinit var downloadListViewModel: DownloadListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDownloadListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViewModel()
        downloadListViewModel.getDownloadData().observe(this) {
            binding.recycleView.adapter = DownloadListAdapter(it!!)
        }
    }

    private fun initializeViewModel(){
        downloadListViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DownloadListViewModel(this@DownloadListActivity) as T
            }
        })[DownloadListViewModel::class.java]
    }

    inner class DownloadListAdapter(
        private val data: List<Video>
    ) : ListAdapter<Video, DownloadListAdapter.DownloadListViewHolder>(
        DOWNLOAD_COMPARATOR
    ) {

        inner class DownloadListViewHolder(private val binding: DownloadItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            val deleteButton: Button = binding.deleteButton
            val cancelButton: Button = binding.cancelButton
            val pauseButton: Button = binding.pauseButton
            val resumeButton: Button = binding.resumeButton
            val thumbnail: ImageView = binding.thumbnail

            fun bind(video: Video) {
                binding.title.text = video.title
                thumbnail.setImageBitmap(video.getLocalThumbnail(applicationContext))
                binding.downloadImage.setImageResource(getDownloadImage(video.downloadState))
                binding.duration.text = video.duration
                binding.percentage.text = "${video.percentageDownloaded} %"
                showOrHideButtons(video.downloadState)
            }

            private fun getDownloadImage(videoState: DownloadStatus?): Int {
                return when (videoState) {
                    DownloadStatus.DOWNLOADING -> com.tpstream.player.R.drawable.ic_baseline_downloading_24
                    DownloadStatus.PAUSE -> com.tpstream.player.R.drawable.ic_baseline_pause_circle_filled_24
                    else -> com.tpstream.player.R.drawable.ic_baseline_file_download_done_24
                }
            }

            private fun showOrHideButtons(videoState: DownloadStatus?) {
                when (videoState) {
                    DownloadStatus.DOWNLOADING -> {
                        deleteButton.visibility = View.GONE
                        cancelButton.visibility = View.VISIBLE
                        pauseButton.visibility = View.VISIBLE
                        resumeButton.visibility = View.GONE
                    }
                    DownloadStatus.PAUSE -> {
                        deleteButton.visibility = View.GONE
                        cancelButton.visibility = View.VISIBLE
                        pauseButton.visibility = View.GONE
                        resumeButton.visibility = View.VISIBLE
                    }
                    DownloadStatus.COMPLETE -> {
                        deleteButton.visibility = View.VISIBLE
                        cancelButton.visibility = View.GONE
                        pauseButton.visibility = View.GONE
                        resumeButton.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadListViewHolder {
            return DownloadListViewHolder(
                DownloadItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: DownloadListViewHolder, position: Int) {
            val video = data[position]
            holder.bind(video)
            holder.deleteButton.setOnClickListener { downloadListViewModel.deleteDownload(video) }
            holder.cancelButton.setOnClickListener { downloadListViewModel.cancelDownload(video) }
            holder.pauseButton.setOnClickListener { downloadListViewModel.pauseDownload(video) }
            holder.resumeButton.setOnClickListener { downloadListViewModel.resumeDownload(video) }
            holder.thumbnail.setOnClickListener {
                if (video.downloadState == DownloadStatus.COMPLETE) {
                    playVideo(video)
                } else {
                    Toast.makeText(applicationContext, "Downloading", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun playVideo(video: Video) {
            val intent = Intent(this@DownloadListActivity, PlayerActivity::class.java)
            intent.putExtra(
                TP_OFFLINE_PARAMS,
                TpInitParams.createOfflineParams(video.videoId)
            )
            startActivity(intent)
        }

        override fun getItemCount() = data.size
    }

    companion object {

        private val DOWNLOAD_COMPARATOR = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean = oldItem.videoId == newItem.videoId
        }
    }
}