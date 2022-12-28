package com.tpstream.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tpstream.app.databinding.DownloadItemBinding
import com.tpstream.player.TpInitParams
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.OfflineVideoState
import com.tpstream.player.models.getLocalThumbnail

class DownloadListActivity : AppCompatActivity() {

    lateinit var viewModel: DownloadListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_list)

        viewModel = DownloadListViewModel(this)

        val listview = findViewById<RecyclerView>(R.id.recycleView)

        viewModel.getDownloadData().observe(this) {
            listview.adapter = DownloadListAdapter(it!!)
        }

    }

    inner class DownloadListAdapter(
        private val data:List<OfflineVideoInfo>
    ) : ListAdapter<OfflineVideoInfo,DownloadListAdapter.DownloadListViewHolder>(DOWNLOAD_COMPARATOR) {

        inner class DownloadListViewHolder(private val binding: DownloadItemBinding) : RecyclerView.ViewHolder(binding.root) {

            val deleteButton : Button = binding.deleteButton
            val cancelButton : Button = binding.cancelButton
            val pauseButton : Button = binding.pauseButton
            val resumeButton : Button = binding.resumeButton
            val thumbnail : ImageView = binding.thumbnail

            fun bind(offlineVideoInfo: OfflineVideoInfo) {
                binding.title.text = offlineVideoInfo.title
                thumbnail.setImageBitmap(offlineVideoInfo.getLocalThumbnail(applicationContext))
                binding.downloadImage.setImageResource(getDownloadImage(offlineVideoInfo.downloadState))
                binding.duration.text = offlineVideoInfo.duration
                binding.percentage.text = "${offlineVideoInfo.percentageDownloaded} %"
                updateButtonVisibility(offlineVideoInfo.downloadState)
            }

            private fun getDownloadImage(offlineVideoState: OfflineVideoState?): Int {
                return when (offlineVideoState) {
                    OfflineVideoState.DOWNLOADING -> com.tpstream.player.R.drawable.ic_baseline_downloading_24
                    OfflineVideoState.PAUSE -> com.tpstream.player.R.drawable.ic_baseline_pause_circle_filled_24
                    else -> com.tpstream.player.R.drawable.ic_baseline_file_download_done_24
                }
            }

            private fun updateButtonVisibility(offlineVideoState: OfflineVideoState?) {
                when (offlineVideoState) {
                    OfflineVideoState.DOWNLOADING -> {
                        deleteButton.visibility = View.GONE
                        cancelButton.visibility = View.VISIBLE
                        pauseButton.visibility = View.VISIBLE
                        resumeButton.visibility = View.GONE
                    }
                    OfflineVideoState.PAUSE -> {
                        deleteButton.visibility = View.GONE
                        cancelButton.visibility = View.VISIBLE
                        pauseButton.visibility = View.GONE
                        resumeButton.visibility = View.VISIBLE
                    }
                    OfflineVideoState.COMPLETE -> {
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
            val offlineVideoInfo = data[position]
            holder.bind(offlineVideoInfo)
            holder.deleteButton.setOnClickListener { viewModel.deleteDownload(offlineVideoInfo) }
            holder.cancelButton.setOnClickListener { viewModel.cancelDownload(offlineVideoInfo) }
            holder.pauseButton.setOnClickListener { viewModel.pauseDownload(offlineVideoInfo) }
            holder.resumeButton.setOnClickListener { viewModel.resumeDownload(offlineVideoInfo) }
            holder.thumbnail.setOnClickListener {
                if (offlineVideoInfo.downloadState == OfflineVideoState.COMPLETE){
                    playVideo(offlineVideoInfo)
                } else {
                    Toast.makeText(applicationContext,"Downloading",Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun playVideo(offlineVideoInfo: OfflineVideoInfo){
            val intent = Intent(this@DownloadListActivity,PlayerActivity::class.java)
            intent.putExtra(TP_OFFLINE_PARAMS,TpInitParams.createOfflineParams(offlineVideoInfo.videoId))
            startActivity(intent)
        }

        override fun getItemCount() = data.size
    }

    companion object {

        private val DOWNLOAD_COMPARATOR = object : DiffUtil.ItemCallback<OfflineVideoInfo>() {
            override fun areItemsTheSame(
                oldItem: OfflineVideoInfo,
                newItem: OfflineVideoInfo
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: OfflineVideoInfo,
                newItem: OfflineVideoInfo
            ): Boolean = oldItem.videoId == newItem.videoId
        }
    }
}