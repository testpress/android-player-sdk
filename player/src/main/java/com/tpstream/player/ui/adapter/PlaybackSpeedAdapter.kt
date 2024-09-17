package com.tpstream.player.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tpstream.player.constants.PlaybackSpeed
import com.tpstream.player.databinding.PlaybackSpeedListItemBinding

internal class PlaybackSpeedAdapter(
    private val context: Context,
    private val selectedSpeed: Float,
    private val items: List<PlaybackSpeed>,
    private val onItemClick: (PlaybackSpeed) -> Unit
) : RecyclerView.Adapter<PlaybackSpeedAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: PlaybackSpeedListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val check: ImageView = binding.check
        val text: TextView = binding.text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PlaybackSpeedListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playbackSpeed = items[position]
        holder.text.text = if (playbackSpeed.value == 1.0f) "Normal" else playbackSpeed.text

        val isSelected = selectedSpeed == playbackSpeed.value
        holder.check.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

        holder.itemView.setOnClickListener {
            onItemClick(playbackSpeed)
        }
    }

    override fun getItemCount(): Int = items.size
}
