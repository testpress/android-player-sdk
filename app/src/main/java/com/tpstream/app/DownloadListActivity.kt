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
import com.tpstream.player.data.Asset
import com.tpstream.player.data.source.local.DownloadStatus

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

        binding.deleteAllButton.setOnClickListener {
            downloadListViewModel.deleteAllDownload()
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
        private val data: List<Asset>
    ) : ListAdapter<Asset, DownloadListAdapter.DownloadListViewHolder>(
        DOWNLOAD_COMPARATOR
    ) {

        inner class DownloadListViewHolder(private val binding: DownloadItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            val deleteButton: Button = binding.deleteButton
            val cancelButton: Button = binding.cancelButton
            val pauseButton: Button = binding.pauseButton
            val resumeButton: Button = binding.resumeButton
            val thumbnail: ImageView = binding.thumbnail

            fun bind(asset: Asset) {
                binding.title.text = asset.title
                thumbnail.setImageBitmap(asset.getLocalThumbnail(applicationContext))
                binding.downloadImage.setImageResource(getDownloadImage(asset.video.downloadState))
                binding.duration.text = asset.video.duration.toString()
                binding.percentage.text = "${asset.video.percentageDownloaded} %"
                showOrHideButtons(asset.video.downloadState)
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
            val asset = data[position]
            holder.bind(asset)
            holder.deleteButton.setOnClickListener { downloadListViewModel.deleteDownload(asset) }
            holder.cancelButton.setOnClickListener { downloadListViewModel.cancelDownload(asset) }
            holder.pauseButton.setOnClickListener { downloadListViewModel.pauseDownload(asset) }
            holder.resumeButton.setOnClickListener { downloadListViewModel.resumeDownload(asset) }
            holder.thumbnail.setOnClickListener {
                if (asset.video.downloadState == DownloadStatus.COMPLETE) {
                    playVideo(asset)
                } else {
                    Toast.makeText(applicationContext, "Downloading", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun playVideo(asset: Asset) {
            val intent = Intent(this@DownloadListActivity, PlayerActivity::class.java)
            intent.putExtra(
                TP_OFFLINE_PARAMS,
                TpInitParams.createOfflineParams(asset.id)
            )
            startActivity(intent)
        }

        override fun getItemCount() = data.size
    }

    companion object {

        private val DOWNLOAD_COMPARATOR = object : DiffUtil.ItemCallback<Asset>() {
            override fun areItemsTheSame(
                oldItem: Asset,
                newItem: Asset
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: Asset,
                newItem: Asset
            ): Boolean = oldItem.id == newItem.id
        }
    }
}