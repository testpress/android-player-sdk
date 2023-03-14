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
            val params = hashMapOf<String,String>(
                "C3XLe1CCcOq" to "demoveranda/c381512b-7337-4d8e-a8cf-880f4f08fd08",
                "o7pOsacWaJt" to "demoveranda/143a0c71-567e-4ecd-b22d-06177228c25b",
                "qJQlWGLJvNv" to "demoveranda/70f61402-3724-4ed8-99de-5473b2310efe",
                "d19729f0-8823-4805-9034-2a7ea9429195" to "edee9b/565a5b8c-310a-444b-956e-bbd6c7c74d7b",
                "73633fa3-61c6-443c-b625-ac4e85b28cfc" to "edee9b/4b11bf9e-d6b7-4b1f-80b8-19d92b26e966"
            )
            val orgCodeAndAccessToken = params[video.videoId]!!.split("/")
            val intent = Intent(this@DownloadListActivity, PlayerActivity::class.java)
            intent.putExtra(
                TP_OFFLINE_PARAMS,
                TpInitParams.createOfflineParams(
                    videoId = video.videoId,
                    orgCode = orgCodeAndAccessToken[0],
                    accessToken = orgCodeAndAccessToken[1])
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